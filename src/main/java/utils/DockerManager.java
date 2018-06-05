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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DockerManager {
    private static DockerManager ourInstance = new DockerManager();
    private Logger logger = LoggerFactory.getLogger(DockerManager.class);
    private Socket socket;
    private Map<Long, String> containerNames = new ConcurrentHashMap<>();

    private DockerManager() {
        try {
            socket = IO.socket("http://195.148.127.246:3000");
            socket.on("start.status", args -> {
                if (args[0] instanceof JSONObject) {
                    JSONObject json = (JSONObject) args[0];
                    logger.info(json.toString());
                    try {
                        String name = json.get("name").toString();
                        String ip = json.get("id").toString();
                        String arguments = json.get("arguments").toString();
                        long id = matchId(arguments);
                        containerNames.put(id, name);
                    } catch (JSONException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    logger.warn("Unexpected: " + args.toString());
                }
            });
            socket.on("stop.status", args -> {
                if (args[0] instanceof JSONObject) {
                    JSONObject json = (JSONObject) args[0];
                    logger.info(json.toString());
                } else {
                    logger.warn("Unexpected: " + args.toString());
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
//        testContainers();
        testRegex();
    }

    private static void testRegex() throws Exception {
        DockerManager manager = new DockerManager();
        String str = " -e job_id=12345 -e host=195.1$8.125.212 -e functionality=service -e services=WebrtcReceiverService -e id=-123 --net host --privileged=true";
        long id = manager.matchId(str);
        System.out.println(id);
    }

    private static void testContainers() throws Exception {
        Map<String, List<Object>> map = new HashMap<>();
        map.put("producer", new ArrayList<>());
        map.put("consumer", new ArrayList<>());
        //client
        Map<String, String> data = getInstance().startContainerData("195.148.125.214", ImmutableMap.<String, String>builder().put("roles", "client").put("haha", "haha").build(), "johnson163/hermes-arm", 8080, "client");
        map.get("producer").add(data);
        //consumer
        data = getInstance().startContainerData("195.148.125.212", ImmutableMap.<String, String>builder().put("roles", "consumer").build(), "johnson163/hermes", 8080, "consumer");
        map.get("consumer").add(data);
        //producer
        data = getInstance().startContainerData("195.148.125.215", ImmutableMap.<String, String>builder().put("roles", "producer").build(), "johnson163/hermes-cuda", 8080, "producer");
        map.get("producer").add(data);
        getInstance().socket.emit("start_containers", map);

        Thread.sleep(8000);

        Map<String, List<Map<String, String>>> terminateMap = new HashMap<>();
        terminateMap.put("common", new ArrayList<>());
//        getInstance().socket.emit("stop_containers", terminateMap);

        System.out.println("finished");
    }

    public Map<Long, String> getContainerNames() {
        return containerNames;
    }

    public void startContainer(Map<String, List<Map<String, String>>> map) {
        socket.emit("start_containers", map);
    }

    public Map<String, String> startContainerData(String ip, Map<String, String> env, String image, int port, String type) {
        logger.info(String.format("Start container in: %s, with image: %s, env: %s", ip, image, env));
        Map<String, String> args = new HashMap<>();
        String envString = "";
        for (String key : env.keySet()) {
            envString += String.format(" -e %s=%s", key, env.get(key));
        }
        if (type.equals("consumer")) {
            args.put("ip", "");
            args.put("port", Integer.toString(port));
            args.put("network", "195.148.125.0");
        } else {
            args.put("ip", ip);
        }
        envString += " --net host --privileged=true";
        args.put("arguments", envString);
        args.put("image", image);
        return args;
    }

    private long matchId(String str) {
        Pattern pattern = Pattern.compile(" id=-?\\d+");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(0).substring(4));
        }
        return -1;
    }

    public void stopContainerAction(String name, String ip) {
        Map<String, List<Map<String, String>>> terminateMap = new HashMap<>();
        Map<String, String> map = stopContainer(name, ip);
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        terminateMap.put("common", list);
        getInstance().socket.emit("stop_containers", terminateMap);
    }

    private Map<String, String> stopContainer(String name, String ip) {
        Map<String, String> args = new HashMap<>();
        args.put("name", name);
        args.put("ip", ip);
        return args;
    }
}
