package com.hzgc.ftpserver.local;

import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.ftpserver.producer.FaceObject;
import com.hzgc.ftpserver.producer.ProducerOverFtp;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.ftpserver.util.IpAddressUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.rocketmq.util.RocketMQProducer;
import org.apache.ftpserver.command.AbstractCommand;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.impl.*;
import org.apache.ftpserver.util.IoUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Map;

public class LocalSTOR extends AbstractCommand {
    private final Logger LOG = LoggerFactory.getLogger(LocalSTOR.class);

    /**
     * Execute command.
     */
    @Override
    public void execute(final FtpIoSession session,
                        final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {
        LocalFtpServerContext localContext = null;
        if (context instanceof LocalFtpServerContext) {
            localContext = (LocalFtpServerContext) context;
        }
        try {

            // get state variable
            long skipLen = session.getFileOffset();

            // argument check
            String fileName = request.getArgument();
            if (fileName == null) {
                session
                        .write(LocalizedFtpReply
                                .translate(
                                        session,
                                        request,
                                        localContext,
                                        FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                                        "STOR", null));
                return;
            }

            // 24-10-2007 - added check if PORT or PASV is issued, see
            // https://issues.apache.org/jira/browse/FTPSERVER-110
            DataConnectionFactory connFactory = null;
            try {
                connFactory = session.getDataConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (connFactory instanceof ServerDataConnectionFactory) {
                InetAddress address = ((ServerDataConnectionFactory) connFactory)
                        .getInetAddress();
                if (address == null) {
                    session.write(new DefaultFtpReply(
                            FtpReply.REPLY_503_BAD_SEQUENCE_OF_COMMANDS,
                            "PORT or PASV must be issued first"));
                    return;
                }
            }

            // get filename
            FtpFile file = null;
            try {
                file = session.getFileSystemView().getFile(fileName);
            } catch (Exception ex) {
                LOG.info("Exception getting file object", ex);
            }
            if (file == null) {
                session.write(LocalizedFtpReply.translate(session, request, localContext,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.invalid", fileName));
                return;
            }
            fileName = file.getAbsolutePath();

            // get permission
            if (!file.isWritable()) {
                session.write(LocalizedFtpReply.translate(session, request, localContext,
                        FtpReply.REPLY_550_REQUESTED_ACTION_NOT_TAKEN,
                        "STOR.permission", fileName));
                return;
            }

            // get data connection
            session.write(
                    LocalizedFtpReply.translate(session, request, localContext,
                            FtpReply.REPLY_150_FILE_STATUS_OKAY, "STOR",
                            fileName)).awaitUninterruptibly(10000);

            LocalIODataConnection dataConnection;
            try {
                IODataConnectionFactory customConnFactory = (IODataConnectionFactory) session.getDataConnection();
                dataConnection = new LocalIODataConnection(customConnFactory.createDataSocket(), customConnFactory.getSession(), customConnFactory);
            } catch (Exception e) {
                LOG.info("Exception getting the input data stream", e);
                session.write(LocalizedFtpReply.translate(session, request, localContext,
                        FtpReply.REPLY_425_CANT_OPEN_DATA_CONNECTION, "STOR",
                        fileName));
                return;
            }

            // transfer data
            boolean failure = false;
            OutputStream outStream = null;
            try {
                outStream = file.createOutputStream(skipLen);
                ProducerOverFtp kafkaProducer = localContext.getProducerOverFtp();
                RocketMQProducer rocketMQProducer = localContext.getProducerRocketMQ();
                InputStream is = dataConnection.getDataInputStream();
                ByteArrayOutputStream baos = FtpUtil.inputStreamCacher(is);
                byte[] data = baos.toByteArray();

                int faceNum = FtpUtil.pickPicture(fileName);

                if (fileName.contains("unknown")) {
                    LOG.error(fileName + ": contain unknown ipcID, Not send to rocketMQ and Kafka!");
                } else {
                    //当FTP接收到小图
                    if (fileName.contains(".jpg") && faceNum > 0) {
                        Map<String, String> map = FtpUtil.getRowKeyMessage(fileName);
                        //若获取不到信息，则不发rocketMQ和Kafka
                        if (!map.isEmpty()) {
                            String ipcID = map.get("ipcID");
                            String timeStamp = map.get("time");
                            String date = map.get("date");
                            String timeSlot = map.get("sj");

                            //发送到rocketMQ
                            SendResult tempResult = rocketMQProducer.send(ipcID, timeStamp, data);
                            rocketMQProducer.send(rocketMQProducer.getMessTopic(), ipcID, timeStamp, tempResult.getOffsetMsgId().getBytes(), null);

                            FaceObject faceObject = new FaceObject();
                            faceObject.setIpcId(ipcID);
                            faceObject.setTimeStamp(timeStamp);
                            faceObject.setTimeSlot(timeSlot);
                            faceObject.setDate(date);
                            faceObject.setType(SearchType.PERSON);
                            faceObject.setAttribute(FaceFunction.featureExtract(data));

                            //发送到kafka
                            String ftpUrl = FtpUtil.filePath2absolutePath(fileName);
                            kafkaProducer.sendKafkaMessage(ProducerOverFtp.getFEATURE(), ftpUrl, faceObject);
                            LOG.info("send to kafka successfully! {}", ftpUrl);
                        }
                    }
                }

                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                long transSz = dataConnection.transferFromClient(session.getFtpletSession(), new BufferedInputStream(bais), outStream);

                // attempt to close the output stream so that errors in
                // closing it will return an error to the client (FTPSERVER-119)
                if (outStream != null) {
                    outStream.close();
                }

                LOG.info("File uploaded {}", fileName);

                // notify the statistics component
                ServerFtpStatistics ftpStat = (ServerFtpStatistics) localContext
                        .getFtpStatistics();
                ftpStat.setUpload(session, file, transSz);

            } catch (SocketException ex) {
                LOG.info("Socket exception during data transfer", ex);
                failure = true;
                session.write(LocalizedFtpReply.translate(session, request, localContext,
                        FtpReply.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                        "STOR", fileName));
            } catch (IOException ex) {
                LOG.info("IOException during data transfer", ex);
                failure = true;
                session
                        .write(LocalizedFtpReply
                                .translate(
                                        session,
                                        request,
                                        localContext,
                                        FtpReply.REPLY_551_REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN,
                                        "STOR", fileName));
            } finally {
                // make sure we really close the output stream
                IoUtils.close(outStream);
            }

            // if data transfer ok - send transfer complete message
            if (!failure) {
                session.write(LocalizedFtpReply.translate(session, request, localContext,
                        FtpReply.REPLY_226_CLOSING_DATA_CONNECTION, "STOR",
                        fileName));

            }
        } finally {
            session.resetState();
            session.getDataConnection().closeDataConnection();
        }
    }
}
