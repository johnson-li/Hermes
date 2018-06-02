package services;

import core.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Protocol;
import utils.ProcessReader;
import utils.ThreadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebrtcSenderService implements Service {
    static Logger logger = LoggerFactory.getLogger(WebrtcSenderService.class);
    private final long id;
    private boolean sendOnly;
    private String remotePeer = "";
    private Process process;
    private ProcessReader reader;

    public WebrtcSenderService(long id) {
        this.id = id;
    }

    public void setRemotePeer(String remotePeer) {
        this.remotePeer = remotePeer;
    }

    @Override
    public void start() {
        // Disable webrtc client
        List<String> commands = new ArrayList<>();
        commands.add("/hermes/bin/webrtc/peerconnection_client_terminal");
        Map<String, String> env = new HashMap<>();
        env.put("server", Config.COORDINATOR_IP);
        env.put("name", String.valueOf(id));
//        env.put("peer", remotePeer);
        env.put("autocall", "true");
//        env.put("sendonly", String.valueOf(sendOnly).toLowerCase());
        env.forEach((key, val) -> {
            commands.add("--" + key);
            commands.add(val);
        });
        reader = ProcessReader.read(commands);
        process = reader.getProcess();
    }

    @Override
    public proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.WebRTC).build();
    }

    @Override
    public void stop() {
        reader = ThreadUtils.stop(reader);
        reader = ThreadUtils.stop(reader);
        process = ThreadUtils.stop(process);
    }
}
