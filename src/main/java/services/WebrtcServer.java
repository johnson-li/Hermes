package services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Protocol;
import proto.hermes.WebrtcServerGrpc;
import utils.ProcessReader;
import utils.ThreadUtils;

@DefaultRun
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
        String cmd = "/hermes/bin/webrtc/peerconnection_server";
        reader = ProcessReader.read(cmd);
        process = reader.getProcess();
    }

    @Override
    public proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.WebRTC).build();
    }

    @Override
    public void stop() {
        reader = ThreadUtils.stop(reader);
        process = ThreadUtils.stop(process);
    }
}
