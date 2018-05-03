package services;

import core.Config;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.*;
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
    private Thread workerThread;

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
        // Disable webrtc client
        if (true) {
            return;
        }
        List<String> commands = new ArrayList<>();
        commands.add("/hermes/bin/webrtc/peerconnection_client_terminal");
        Map<String, String> env = new HashMap<>();
        env.put("server", Config.COORDINATOR_IP);
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
        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (observer != null) {
                    observer.onNext(WebrtcResponse.newBuilder().setStatus(Status.SUCCESS).build());
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        workerThread.start();
    }

    @Override
    public void init() {

    }

    @Override
    public proto.hermes.Service getService() {
        return proto.hermes.Service.newBuilder().setName(getName()).setProtocol(Protocol.WebRTC).build();
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
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
            workerThread = null;
        }
    }
}
