package services;

import core.Config;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Status;
import proto.hermes.WebrtcGrpc;
import proto.hermes.WebrtcInfo;
import proto.hermes.WebrtcResponse;
import utils.ProcessReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebrtcSenderService extends WebrtcGrpc.WebrtcImplBase implements Service {
    static Logger logger = LoggerFactory.getLogger(WebrtcSenderService.class);
    private final long id;
    private final boolean sendOnly;
    private String remotePeer = "";
    private Process process;
    private ProcessReader reader;
    private StreamObserver<WebrtcResponse> observer;

    public WebrtcSenderService(long id, boolean sendOnly) {
        this.id = id;
        this.sendOnly = sendOnly;
    }

    @Override
    public void webrtc(WebrtcInfo request, StreamObserver<WebrtcResponse> responseObserver) {
        observer = responseObserver;
    }

    @Override
    public void listen(ManagedChannel channel) {

    }

    public void setRemotePeer(String remotePeer) {
        this.remotePeer = remotePeer;
    }

    @Override
    public void start() {
        List<String> commands = new ArrayList<>();
        commands.add("/hermes/bin/webrtc/peerconnection_client_terminal");
        Map<String, String> env = new HashMap<>();
        env.put("coordinator_ip", Config.COORDINATOR_IP);
        env.put("name", String.valueOf(id));
        env.put("peer", remotePeer);
        env.put("autocall", "true");
        env.put("sendonly", String.valueOf(sendOnly).toLowerCase());
        env.forEach((key, val) -> {
            commands.add("--" + key);
            commands.add(val);
        });
        try {
            process = new ProcessBuilder(commands).start();
            reader = new ProcessReader(process);
            reader.start();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        new Thread(() -> {
            while (true) {
                if (observer != null) {
                    observer.onNext(WebrtcResponse.newBuilder().setStatus(Status.SUCCESS).build());
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
            process = null;
        }
        if (reader != null && reader.isAlive()) {
            reader.interrupt();
            reader = null;
        }
    }
}
