package utils;

public class ThreadUtils {

    public static Process stop(Process process) {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
        return null;
    }

    public static ProcessReader stop(ProcessReader processReader) {
        if (processReader != null && !processReader.isInterrupted()) {
            processReader.interrupt();
        }
        return null;
    }

    public static Thread stop(Thread thread) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        return null;
    }
}
