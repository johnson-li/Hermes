package services;

import java.util.HashMap;
import java.util.Map;

public class ServiceManager {
    private Map<String, Service> services = new HashMap<>();

    public Service getService(String name) {
        if (services.containsKey(name)) {
            return services.get(name);
        }
        Service service = null;
        switch (name) {
            case "PrintService":
                service = new PrintService();
                break;
            case "EchoService":
                service = new EchoService();
                break;
            case "RandomGenerator":
                service = new RandomGenerator();
                break;
        }
        services.put(name, service);
        return service;
    }
}
