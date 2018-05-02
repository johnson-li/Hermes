package core;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static String COORDINATOR_IP = "195.148.127.246";
    public static int COORDINATOR_PORT = 5555;
    public static List<String> ROLES = new ArrayList<>();
    public static String HOST = "127.0.0.1";
    public static int PORT = 5555;
    public static int RUNNING_TIME = 20000;

    static {
        try {
            HOST = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        initConfig();
    }

    private static void initConfig() {
        String roles = System.getenv("roles");
        if (valid(roles)) {
            ROLES.addAll(Arrays.asList(roles.split(",")));
        }
        String coordinator = System.getenv("coordinator");
        if (valid(coordinator)) {
            COORDINATOR_IP = coordinator;
        }
        String host = System.getenv("host");
        if (valid(host)) {
            HOST = host;
        }
        String port = System.getenv("port");
        if (valid(port)) {
            PORT = Integer.valueOf(port);
        }
        String runningTime = System.getenv("running_time");
        if (valid(runningTime)) {
            RUNNING_TIME = Integer.valueOf(runningTime);
        }
    }

    private static boolean valid(String str) {
        return str != null && str.length() > 0;
    }
}
