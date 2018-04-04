package services;

import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.WebrtcServerGrpc;
import utils.ProcessReader;

import java.io.IOException;

public class WebrtcServer extends WebrtcServerGrpc.WebrtcServerImplBase implements Service {
    static Logger logger = LoggerFactory.getLogger(WebrtcServer.class);
    Process process;
    ProcessReader reader;

    @Override
    public void listen(ManagedChannel channel) {

    }

    @Override
    public void start() {
        try {
            String cmd = "/hermes/script/webrtc-start-server.sh";
            process = Runtime.getRuntime().exec(cmd);
            reader = new ProcessReader(process);
            reader.start();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (reader != null && reader.isAlive()) {
            reader.interrupt();
            reader = null;
        }
        if (process != null && process.isAlive()) {
            process.destroy();
            process = null;
        }
    }
}
