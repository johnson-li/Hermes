package roles;

import com.google.protobuf.TextFormat;
import core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Task;
import services.RandomGenerator;
import services.Service;
import services.TaskController;

public class Producer extends Role {
    Logger logger = LoggerFactory.getLogger(Producer.class);
    RandomGenerator generator = new RandomGenerator();

    public Producer(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(generator);
        super.init();
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
}
