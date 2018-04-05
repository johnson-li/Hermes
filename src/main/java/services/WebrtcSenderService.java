package services;

import core.Config;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.WebrtcGrpc;

import java.io.IOException;

public class WebrtcSenderService extends WebrtcGrpc.WebrtcImplBase implements Service {
    static Logger logger = LoggerFactory.getLogger(WebrtcSenderService.class);

    @Override
    public void listen(ManagedChannel channel) {

    }

    @Override
    public void start() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("", new String[]{"coordinator_ip=" + Config.COORDINATOR_IP});
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }
}
