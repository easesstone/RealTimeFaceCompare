/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.hzgc.collect.ftp.command.impl;

import com.hzgc.collect.ftp.DataConnectionConfiguration;
import com.hzgc.collect.ftp.command.AbstractCommand;
import com.hzgc.collect.ftp.ftplet.FtpException;
import com.hzgc.collect.ftp.ftplet.FtpRequest;
import com.hzgc.collect.ftp.impl.ServerDataConnectionFactory;
import com.hzgc.collect.ftp.ssl.SslConfiguration;
import com.hzgc.collect.ftp.ftplet.FtpReply;
import com.hzgc.collect.ftp.impl.FtpIoSession;
import com.hzgc.collect.ftp.impl.FtpServerContext;
import com.hzgc.collect.ftp.impl.LocalizedFtpReply;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Data channel protection level.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class PROT extends AbstractCommand {

    private SslConfiguration getSslConfiguration(final FtpIoSession session) {
        DataConnectionConfiguration dataCfg = session.getListener().getDataConnectionConfiguration();
        
        SslConfiguration configuration = dataCfg.getSslConfiguration();

        // fall back if no configuration has been provided on the data connection config
        if(configuration == null) {
            configuration = session.getListener().getSslConfiguration();
        }
        
        return configuration;
    }
    
    /**
     * Execute command.
     */
    public void execute(final FtpIoSession session,
                        final FtpServerContext context, final FtpRequest request)
            throws IOException, FtpException {

        // reset state variables
        session.resetState();

        // check argument
        String arg = request.getArgument();
        if (arg == null) {
            session.write(LocalizedFtpReply.translate(session, request, context,
                    FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                    "PROT", null));
            return;
        }

        // check argument
        arg = arg.toUpperCase();
        ServerDataConnectionFactory dcon = session.getDataConnection();
        if (arg.equals("C")) {
            dcon.setSecure(false);
            session.write(LocalizedFtpReply.translate(session, request, context,
                    FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
        } else if (arg.equals("P")) {
            if (getSslConfiguration(session) == null) {
                session.write(LocalizedFtpReply.translate(session, request, context,
                        431, "PROT", null));
            } else {
                dcon.setSecure(true);
                session.write(LocalizedFtpReply.translate(session, request, context,
                        FtpReply.REPLY_200_COMMAND_OKAY, "PROT", null));
            }
        } else {
            session
                    .write(LocalizedFtpReply
                            .translate(
                                    session,
                                    request,
                                    context,
                                    FtpReply.REPLY_504_COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER,
                                    "PROT", null));
        }
    }

}
