package core;

import services.ServiceManager;

public class Context {
    ServiceManager serviceManager = new ServiceManager();

    public ServiceManager getServiceManager() {
       return serviceManager;
    }
}
