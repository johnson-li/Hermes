package roles;

import core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.EchoService;
import services.Service;
import services.TaskController;
import services.WebrtcSenderService;

public class Consumer extends Role {
    private static Logger logger = LoggerFactory.getLogger(Consumer.class);

    public Consumer(Context context, Service... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(new EchoService());
        addServices(new WebrtcSenderService(context.getId()));
        super.init();
    }
}
