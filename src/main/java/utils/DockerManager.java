package utils;

import com.google.common.collect.ImmutableMap;
import io.socket.client.IO;
import io.socket.client.Socket;
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

    private DockerManager() {
        try {
            socket = IO.socket("http://195.148.127.245:3000");
            socket.on("edge.start.status", args -> {
                System.out.println(args[0].getClass());
                if (args[0] instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) args[0];
                    names.add(map.get("Name").toString());
                }
            });
            socket.on("prod.start.status", args -> {
                System.out.println(args[0].getClass());
                if (args[0] instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) args[0];
                    names.add(map.get("Name").toString());
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

        Thread.sleep(20000);

        Map<String, List<Map<String, String>>> terminateMap = new HashMap<>();
        map.put("producer", new ArrayList<>());
        map.put("consumer", new ArrayList<>());
        getInstance().names.forEach(name -> {
            Map<String, String> wrapped = getInstance().stopContainer(name);
            map.get("consumer").add(wrapped);
        });
        getInstance().socket.emit("stop_containers", terminateMap);
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

    public Map<String, String> stopContainer(String name) {
        Map<String, String> args = new HashMap<>();
        args.put("name", name);
        return args;
    }
}
