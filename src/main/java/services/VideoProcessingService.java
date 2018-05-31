package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ProcessReader;
import utils.ThreadUtils;

public class VideoProcessingService implements Service {
    static Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);
    private Process process;
    private ProcessReader reader;

    @Override
    public void start() {
        reader = ProcessReader.read("cd /hermes/darknet && ./darknet detect cfg/yolov3.cfg yolov3.weights");
        process = reader.getProcess();
    }

    @Override
    public void stop() {
        reader = ThreadUtils.stop(reader);
        process = ThreadUtils.stop(process);
    }
}
