package roles;

import com.google.protobuf.TextFormat;
import core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Task;
import services.EchoService;
import services.Service;
import services.TaskController;

public class Consumer extends Role {
    private static Logger logger = LoggerFactory.getLogger(Consumer.class);
    private EchoService echoService = new EchoService();

    public Consumer(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(echoService);
        super.init();
    }

    @Override
    public void onStopped(Task task) {
        logger.info("On task stopped: " + TextFormat.shortDebugString(task));
        echoService.stop();
    }

    @Override
    public void onStarted(Task task) {
        logger.info("On task started: " + TextFormat.shortDebugString(task));
        echoService.start();
    }
}
