package core;

import jobs.EchoJob;
import jobs.Job;
import participants.Functionality;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    public static String COORDINATOR_IP = "195.148.127.246";
    public static int COORDINATOR_PORT = 5555;
    public static List<String> ROLES = new ArrayList<>();
    public static List<String> SERVICES = new ArrayList<>(); // This is by both the management and the service, the usage is not exactly the same
    public static String HOST = "127.0.0.1";
    public static int PORT = 5555;
    public static int SERVICE_PORT = 5556;
    public static int RUNNING_TIME = 1800000;
    public static int PREPARATION_TIME = 10000;
    public static long ID;
    public static Functionality FUNCTIONALITY = Functionality.MANAGEMENT;
    public static String MANAGEMENT_IP = "127.0.0.1";
    public static long JOB_ID;
    public static boolean AUTO_PLAY = true;
    public static String JOB = Job.getJobName(EchoJob.class);

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
        String services = System.getenv("services");
        if (valid(services)) {
            SERVICES.addAll(Arrays.asList(services.split(",")));
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
        String preparationTime = System.getenv("preparation_time");
        if (valid(preparationTime)) {
            PREPARATION_TIME = Integer.valueOf(preparationTime);
        }
        String id = System.getenv("id");
        if (valid(id)) {
            ID = Long.valueOf(id);
        }
        String functionality = System.getenv("functionality");
        if (valid(functionality)) {
            FUNCTIONALITY = Functionality.valueOf(functionality.toUpperCase());
        }
        String managementIP = System.getenv("management_ip");
        if (valid(managementIP)) {
            MANAGEMENT_IP = managementIP;
        }
        String servicePort = System.getenv("service_port");
        if (valid(servicePort)) {
            SERVICE_PORT = Integer.valueOf(servicePort);
        }
        String jobID = System.getenv("job_id");
        if (valid(jobID)) {
            JOB_ID = Long.valueOf(jobID);
        }
        String autoPlay = System.getenv("auto_play");
        if (valid(autoPlay)) {
            AUTO_PLAY = Boolean.valueOf(autoPlay);
        }
        String job = System.getenv("job");
        if (valid(job)) {
            JOB = job;
        }
    }

    private static boolean valid(String str) {
        return str != null && str.length() > 0;
    }
}
