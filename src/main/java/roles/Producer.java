package roles;

import core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.RandomGenerator;
import services.Service;
import services.TaskController;
import services.WebrtcSenderService;

public class Producer extends Role {
    Logger logger = LoggerFactory.getLogger(Producer.class);

    public Producer(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(new RandomGenerator());
        addServices(new WebrtcSenderService(context.getId()));
        super.init();
    }
}
