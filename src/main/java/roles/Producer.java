package roles;

import com.google.protobuf.TextFormat;
import core.Context;
import io.grpc.BindableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Task;
import services.RandomGenerator;
import services.TaskController;

public class Producer extends Role implements TaskController.TaskListener {
    Logger logger = LoggerFactory.getLogger(Producer.class);
    RandomGenerator generator = new RandomGenerator();

    public Producer(Context context, BindableService... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(generator);
    }

    @Override
    public void onStarted(Task task) {
        logger.info("On task started: " + TextFormat.shortDebugString(task));
        generator.start();
    }

    @Override
    public void onStopped(Task task) {
        logger.info("On task stopped: " + TextFormat.shortDebugString(task));
        generator.stop();
    }

    @Override
    public void onTaskAssigned(Task task) {
        logger.info("On task assigned: " + TextFormat.shortDebugString(task));
    }
}
