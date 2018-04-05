package roles;

import core.Context;
import exception.ServiceNotFoundException;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.hermes.Participant;
import proto.hermes.Task;
import services.Service;
import services.ServiceManager;
import services.TaskListener;
import utils.ChannelUtil;

import javax.net.ssl.SSLException;
import java.util.*;

public abstract class Role implements TaskListener {
    final Context context;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<Service> services = new ArrayList<>();
    private ManagedChannel channel;

    public Role(Context context) {
        this(context, new Service[0]);
    }

    public Role(Context context, Service... services) {
        this.context = context;
        addServices(services);
    }

    public void init() {
        services.forEach(Service::init);
    }

    ServiceManager getServiceManager() {
        return context.getServiceManager();
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public void setChannel(ManagedChannel channel) {
        this.channel = channel;
    }

    public String getRoleName() {
        return getClass().getSimpleName();
    }

    void addServices(Service... services) {
        this.services.addAll(Arrays.asList(services));
    }

    @Override
    public void onStopped(Task task) {
        services.forEach(Service::stop);
    }

    @Override
    public void onStarted(Task task) throws ServiceNotFoundException {
        String service = task.getService().getName();
        getService(service).orElseThrow(() -> new ServiceNotFoundException(service)).start();
    }

    @Override
    public void onTaskAssigned(Task task) {
        for (Participant ingress : task.getIngressesList()) {
            try {
                ManagedChannel channel = ChannelUtil.getInstance()
                        .newClientChannel(ingress.getAddress().getIp(), ingress.getAddress().getPort());
                String serviceName = task.getService().getName();
                Service service = getService(serviceName).orElseThrow(() -> new ServiceNotFoundException(serviceName));
                ChannelUtil.getInstance().getWorkingGroup().execute(() -> service.listen(channel));
            } catch (SSLException | ServiceNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Optional<Service> getService(String name) {
        return services.stream().filter(service -> service.getClass().getSimpleName().equals(name)).findFirst();
    }

    public Collection<Service> getServices() {
        return services;
    }
}
