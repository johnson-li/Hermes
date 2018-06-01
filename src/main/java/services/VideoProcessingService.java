package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ProcessReader;
import utils.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class VideoProcessingService implements Service {
    static Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);
    private Process process;
    private ProcessReader reader;

    @Override
    public void start() {
        List<String> commands = new ArrayList<>();
        commands.add("/hermes/darknet/darknet");
        commands.add("detect");
        commands.add("/hermes/darknet/cfg/yolov3.cfg");
        commands.add("/hermes/darknet/yolov3.weights");
        reader = ProcessReader.read(commands);
        process = reader.getProcess();
    }

    @Override
    public void stop() {
        reader = ThreadUtils.stop(reader);
        process = ThreadUtils.stop(process);
    }
}
