package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ProcessReader extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ProcessReader.class);
    private byte[] buffer = new byte[1024 * 1024];
    private Process process;

    public ProcessReader(Process process) {
        this.process = process;
    }

    public static ProcessReader read(String commands) {
        return read(Collections.singletonList(commands));
    }

    public static ProcessReader read(List<String> commands) {
        return read(commands, "/");
    }

    public static ProcessReader read(List<String> commands, String path) {
        try {
            logger.info("RUN " + commands + " in " + path);
            Process process = new ProcessBuilder(commands).directory(new File(path)).start();
            return read(process);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    static ProcessReader read(Process process) {
        ProcessReader reader;
        reader = new ProcessReader(process);
        reader.start();
        return reader;
    }

    public Process getProcess() {
        return process;
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

    @Override
    public void interrupt() {
        super.interrupt();
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }
}
