package utils;

import com.google.common.collect.ImmutableMap;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerManager {
    private static DockerManager ourInstance = new DockerManager();
    private Logger logger = LoggerFactory.getLogger(DockerManager.class);
    private Socket socket;
    private List<String> names = new ArrayList<>();
    private List<String> ips = new ArrayList<>();

    private DockerManager() {
        try {
            socket = IO.socket("http://195.148.127.245:3000");
            socket.on("start.status", args -> {
                if (args[0] instanceof JSONObject) {
                    JSONObject json = (JSONObject) args[0];
                    logger.info(json.toString());
                    try {
                        String name = json.get("name").toString();
                        String ip = json.get("id").toString();
                        names.add(name);
                        ips.add(ip);
                    } catch (JSONException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    System.out.println(args[0].getClass());
                }
            });
            socket.on("stop.status", args -> {
                System.out.println(args[0].getClass());
                System.out.println();
                if (args[0] instanceof JSONObject) {
                    JSONObject json = (JSONObject) args[0];
                    logger.info(json.toString());
                } else {
                    System.out.println(args[0].getClass());
                }
            });
            socket.connect();
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static DockerManager getInstance() {
        return ourInstance;
    }

    public static void main(String[] args) throws Exception {
        Map<String, List<Object>> map = new HashMap<>();
        map.put("producer", new ArrayList<>());
        map.put("consumer", new ArrayList<>());
        //client
        Map<String, Object> data = getInstance().startContainer("195.148.125.214", ImmutableMap.<String, String>builder().put("roles", "client").build(), "johnson163/hermes-arm", 8080, "client");
        map.get("producer").add(data);
        //consumer
        data = getInstance().startContainer("195.148.125.212", ImmutableMap.<String, String>builder().put("roles", "consumer").build(), "johnson163/hermes", 8080, "consumer");
        map.get("consumer").add(data);
        //producer
        data = getInstance().startContainer("195.148.125.213", ImmutableMap.<String, String>builder().put("roles", "producer").build(), "johnson163/hermes-arm", 8080, "producer");
        map.get("producer").add(data);
        getInstance().socket.emit("start_containers", map);

        Thread.sleep(8000);

        Map<String, List<Map<String, String>>> terminateMap = new HashMap<>();
        terminateMap.put("common", new ArrayList<>());
        for (int i = 0; i < getInstance().names.size(); i++) {
            String name = getInstance().names.get(i);
            String ip = getInstance().ips.get(i);
            Map<String, String> wrapped = getInstance().stopContainer(name, ip);
            terminateMap.get("common").add(wrapped);
        }
        getInstance().socket.emit("stop_containers", terminateMap);

        System.out.println("finished");
    }

    public Map<String, Object> startContainer(String ip, Map<String, String> env, String image, int port, String type) {
        Map<String, Object> args = new HashMap<>();
        String envString = "";
        for (String key : env.keySet()) {
            envString += String.format(" -e %s=%s", key, env.get(key));
        }
        if (type.equals("consumer")) {
            args.put("ip", "");
            args.put("port", Integer.toString(port));
        } else {
            args.put("ip", ip);
            envString += String.format(" -p %s:%s", port, port);
        }
//        args.put("env", env);
        args.put("arguments", envString);
        args.put("image", image);
//        args.put("type", type);
        return args;
    }

    public Map<String, String> stopContainer(String name, String ip) {
        Map<String, String> args = new HashMap<>();
        args.put("name", name);
        args.put("ip", ip);
        return args;
    }
}
