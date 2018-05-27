package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Protocol;
import proto.hermes.WebrtcServerGrpc;
import utils.ProcessReader;

import java.io.IOException;

public class WebrtcServer extends WebrtcServerGrpc.WebrtcServerImplBase implements Service {
    private static Logger logger = LoggerFactory.getLogger(WebrtcServer.class);
    private Process process;
    private ProcessReader reader;

    @Override
    public void init() {
        // Disable webrtc in the development environment
        if (System.getProperty("os.name").equals("Mac OS X")) {
            logger.warn("Disable webrtc in the development environment");
            return;
        }
        try {
            String cmd = "/hermes/bin/webrtc/peerconnection_server";
            process = Runtime.getRuntime().exec(cmd);
            reader = new ProcessReader(process);
            reader.start();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.WebRTC).build();
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
