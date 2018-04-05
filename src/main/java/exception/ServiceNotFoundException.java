package exception;

public class ServiceNotFoundException extends Exception{
    public ServiceNotFoundException(String serviceName) {
        super("Service not found: " + serviceName);
    }
}
