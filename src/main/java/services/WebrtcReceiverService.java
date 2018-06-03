package services;


import com.google.common.base.Strings;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import proto.hermes.*;
import utils.ChannelUtil;
import utils.ProcessReaderListener;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebrtcReceiverService extends WebrtcGrpc.WebrtcImplBase implements Service, ProcessReaderListener {
    private final WebrtcSenderService senderService;
    private final VideoProcessingService processingService = new VideoProcessingService(this);
    private final Runnable workerThread;
    private StreamObserver<WebrtcResponse> observer;
    private boolean interrupted;

    public WebrtcReceiverService(long id) {
        senderService = new WebrtcSenderService(id);
        workerThread = new Runnable() {
            @Override
            public void run() {
                if (!interrupted) {
                    if (observer != null) {
                        observer.onNext(WebrtcResponse.newBuilder().setStatus(Status.SUCCESS).build());
                    }
                    ChannelUtil.getInstance().execute(workerThread, 10000);
                }
            }
        };
    }

    public static void main(String[] args) {
        WebrtcReceiverService service = new WebrtcReceiverService(0);
        service.onReadLine("**car: 98.3%, box(11.1,22,33,44)**");
    }

    @Override
    public void onRead(String str) {
        String[] lines = str.split("\n");
        Arrays.stream(lines).forEach(this::onReadLine);
    }

    private void onReadLine(String line) {
        if (Strings.isNullOrEmpty(line)) {
            return;
        }
        line = line.trim();
        if (line.startsWith("**") && line.endsWith("**")) {
            Pattern pattern = Pattern.compile("\\*\\*([a-z]+): (\\d+\\.?\\d*)%, box\\((\\d+\\.?\\d*),(\\d+\\.?\\d*),(\\d+\\.?\\d*),(\\d+\\.?\\d*)\\)\\*\\*");
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                String obj = matcher.group(1);
                float confidence = Float.valueOf(matcher.group(2));
                float x = Float.valueOf(matcher.group(3));
                float y = Float.valueOf(matcher.group(4));
                float width = Float.valueOf(matcher.group(5));
                float height = Float.valueOf(matcher.group(6));
                WebrtcResponse response = WebrtcResponse.newBuilder().setObject(obj).setTs(System.currentTimeMillis())
                        .setBox(Box.newBuilder().setX(x).setY(y).setWidth(width).setHeight(height).build()).build();
                if (observer != null) {
                    observer.onNext(response);
                }
            }
        }
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
