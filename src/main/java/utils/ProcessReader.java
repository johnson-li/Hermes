package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProcessReader extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ProcessReader.class);
    private byte[] buffer = new byte[1024 * 1024];
    private Process process;

    public ProcessReader(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
        while (!interrupted() && process.isAlive()) {
            try {
                if (process.getErrorStream().available() > 0) {
                    int length = process.getErrorStream().read(buffer);
                    logger.error(new String(buffer, 0, length));
                }
                if (process.getInputStream().available() > 0) {
                    int length = process.getInputStream().read(buffer);
                    logger.info(new String(buffer, 0, length));
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
