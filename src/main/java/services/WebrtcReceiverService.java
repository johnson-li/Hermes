package services;


import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import proto.hermes.Status;
import proto.hermes.WebrtcGrpc;
import proto.hermes.WebrtcInfo;
import proto.hermes.WebrtcResponse;
import utils.ChannelUtil;

public class WebrtcReceiverService extends WebrtcGrpc.WebrtcImplBase implements Service {
    private final WebrtcSenderService senderService;
    private final VideoProcessingService processingService = new VideoProcessingService();
    private final Runnable workerThread;
    private StreamObserver<WebrtcResponse> observer;
    private boolean interrupted;

    public WebrtcReceiverService(long id) {
        senderService = new WebrtcSenderService(id);
        workerThread = new Runnable() {
            @Override
            public void run() {
                while (!interrupted) {
                    if (observer != null) {
                        observer.onNext(WebrtcResponse.newBuilder().setStatus(Status.SUCCESS).build());
                    }
                    ChannelUtil.getInstance().execute(workerThread, 10000);
                }
            }
        };
    }

    @Override
    public void webrtc(WebrtcInfo request, StreamObserver<WebrtcResponse> responseObserver) {
        observer = responseObserver;
    }

    @Override
    public void start() {
        logger.info("Start WebrtcReceiverService");
        processingService.start();
        senderService.start();
        ChannelUtil.getInstance().execute(workerThread);
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
        interrupted = true;
    }

    @Override
    public void listen(ManagedChannel channel) {
        processingService.listen(channel);
        senderService.listen(channel);
    }
}
