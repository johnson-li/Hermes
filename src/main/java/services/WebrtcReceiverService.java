package services;


import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import proto.hermes.Status;
import proto.hermes.WebrtcGrpc;
import proto.hermes.WebrtcInfo;
import proto.hermes.WebrtcResponse;

public class WebrtcReceiverService extends WebrtcGrpc.WebrtcImplBase implements Service {
    private final WebrtcSenderService senderService;
    private final VideoProcessingService processingService = new VideoProcessingService();
    private StreamObserver<WebrtcResponse> observer;
    private Thread workerThread = new Thread(() -> {
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

    public WebrtcReceiverService(long id) {
        senderService = new WebrtcSenderService(id);
    }

    @Override
    public void webrtc(WebrtcInfo request, StreamObserver<WebrtcResponse> responseObserver) {
        observer = responseObserver;
    }

    @Override
    public void start() {
        processingService.start();
        senderService.start();
        workerThread.start();
    }

    @Override
    public void init() {
        processingService.init();
        senderService.init();
    }

    @Override
    public void stop() {
        processingService.stop();
        senderService.stop();
        workerThread.interrupt();
    }

    @Override
    public void listen(ManagedChannel channel) {
        processingService.listen(channel);
        senderService.listen(channel);
    }
}
