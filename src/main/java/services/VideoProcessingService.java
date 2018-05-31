package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ProcessReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoProcessingService implements Service {
    static Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);
    private Process process;
    private ProcessReader reader;
    private Thread workerThread;

    @Override
    public void start() {
        List<String> commands = new ArrayList<>();
        commands.add("/hermes/bin/darknet/darknet");
        commands.add("");
        try {
            process = new ProcessBuilder(commands).start();
            reader = new ProcessReader(process);
            reader.start();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void stop() {

    }
}
