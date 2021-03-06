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
    public static String CLIENT_PATH = "/hermes/bin/webrtc/peerconnection_client_terminal";
    static Logger logger = LoggerFactory.getLogger(WebrtcSenderService.class);
    private final long id;
    private boolean sendOnly;
    private String remotePeer = "";
    private Process process;
    private ProcessReader reader;

    public WebrtcSenderService(long id) {
        this.id = id;
    }
//        env.put("sendonly", String.valueOf(sendOnly).toLowerCase());
        env.forEach((key, val) -> {
            commands.add("--" + key);
            commands.add(val);

            public static void main(String[] args) {
                WebrtcSenderService service = new WebrtcSenderService(0xffff);
                service.start();
            }

            public void setRemotePeer(String remotePeer) {
                this.remotePeer = remotePeer;
            }

            @Override
            public void start() {
                String os = System.getProperty("os.name");
                if (os.equals("Mac OS X")) {
                    return;
                }
                // Disable webrtc client
                List<String> commands = new ArrayList<>();
                commands.add(CLIENT_PATH);
                Map<String, String> env = new HashMap<>();
                env.put("server", Config.COORDINATOR_IP);
                env.put("name", String.valueOf(id));
//        env.put("peer", remotePeer);
                env.put("autocall", "true");
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
        process = ThreadUtils.stop(process);
    }
}
