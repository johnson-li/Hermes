package core;

import services.ServiceManager;

public class Context {
    protected final long id;

    public Context(long id) {
        this.id = id;
    }

    ServiceManager serviceManager = new ServiceManager();

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public long getId() {
        return id;
    }
}
