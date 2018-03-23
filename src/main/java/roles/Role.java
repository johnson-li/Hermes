package roles;

import core.Context;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import services.ServiceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class Role {
    private final Context context;
    private List<BindableService> services = new ArrayList<>();
    private ManagedChannel channel;

    public Role(Context context) {
        this(context, new BindableService[0]);
    }

    public Role(Context context, BindableService... services) {
        this.context = context;
        addServices(services);
    }

    public void init() {
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

    public void addServices(BindableService... services) {
        this.services.addAll(Arrays.asList(services));
    }

    public Collection<BindableService> getServices() {
        return services;
    }
}
