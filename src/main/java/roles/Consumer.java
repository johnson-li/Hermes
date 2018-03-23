package roles;

import com.google.protobuf.TextFormat;
import core.Context;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Participant;
import proto.hermes.Task;
import services.EchoService;
import services.TaskController;
import utils.ChannelUtil;

import javax.net.ssl.SSLException;

public class Consumer extends Role implements TaskController.TaskListener {
    private static Logger logger = LoggerFactory.getLogger(Consumer.class);
    EchoService echoService = (EchoService) getServiceManager().getService("EchoService");

    public Consumer(Context context, BindableService... services) {
        super(context, services);
    }

    @Override
    public void init() {
        addServices(new TaskController(this));
        addServices(echoService);
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

    @Override
    public void onTaskAssigned(Task task) {
        for (Participant ingress : task.getIngressesList()) {
            try {
                ManagedChannel channel = ChannelUtil.getInstance()
                        .newClientChannel(ingress.getAddress().getIp(), ingress.getAddress().getPort());
                String serviceName = task.getService().getName();
                ChannelUtil.getInstance().getWorkingGroup().execute(() ->
                        getServiceManager().getService(serviceName).listen(channel));
            } catch (SSLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
