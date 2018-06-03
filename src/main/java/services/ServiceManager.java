package services;

import java.util.HashMap;
import java.util.Map;

public class ServiceManager {
    private Map<String, Service> services = new HashMap<>();

    public Service getService(String name, long id) {
        if (services.containsKey(name)) {
            return services.get(name);
        }
        Service service = null;
        if (name.equals(PrintService.class.getSimpleName())) {
            service = new PrintService();
        } else if (name.equals(EchoService.class.getSimpleName())) {
            service = new EchoService();
        } else if (name.equals(RandomGenerator.class.getSimpleName())) {
            service = new RandomGenerator();
        } else if (name.equals(WebrtcReceiverService.class.getSimpleName())) {
            service = new WebrtcReceiverService(id);
        } else if (name.equals(WebrtcSenderService.class.getSimpleName())) {
            service = new WebrtcSenderService(id);
        } else if (name.equals(WebrtcClientService.class.getSimpleName())) {
            service = new WebrtcClientService();
        }
        if (service != null) {
            services.put(name, service);
        }
        return service;
    }
}
