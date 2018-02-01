package com.hzgc.collect.expand.log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class Main {
    static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Random random = new Random();
        for (int i = 0; i <10 ; i++) {
            System.out.println(String.valueOf(random.nextInt(3)));
        }
    }

    public static String getLastLine(String fileName) {
        RandomAccessFile raf = null;
        try {
            String tempFile = "E:\\getProcessLogDir\\process\\process-25\\0000000000000000810.log";
            raf = new RandomAccessFile(tempFile, "r");
            LOG.info("Start get last line from " + tempFile);
            long length = raf.length();
            long position = length - 1;
            if (position != -1) {
                raf.seek(position);
                while (position >= 0) {
                    int bb = raf.read();
                    if (bb != '\r' && bb != '\n') {
                        break;
                    }
                    if (position == 0) {
                        raf.seek(position);
                        break;
                    } else {
                        position--;
                        raf.seek(position);
                    }
                }
                System.out.println(position);
                if (position >= 0) {
                    while (position >= 0) {
                        if (position == 0) {
                            raf.seek(position);
                            break;
                        } else {
                            position--;
                            raf.seek(position);
                            if (raf.read() == '\n') {
                                break;
                            }
                        }
                    }
                }
                System.out.println(position);
                String line = raf.readLine();
                if (line != null) {
                    LOG.info("Last line from " + tempFile + " is:[" + line + "]");
                    return line;
                } else {
                    LOG.warn("Last line from " + tempFile + " is:[" + null + "], maybe this queue never receives data");
                    return "";
                }
            } else {
                LOG.warn("Last line from " + tempFile + " is:[" + null + "], maybe this queue never receives data");
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
