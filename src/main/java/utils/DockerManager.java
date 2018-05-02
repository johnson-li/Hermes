package utils;

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

    private DockerManager() {
        try {
            socket = IO.socket("http://195.148.127.245:3000");
            socket.on("start_container_result", args -> {
                for (Object arg : args) {
                    logger.info(arg.toString());
                }
            });
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static DockerManager getInstance() {
        return ourInstance;
    }

    public static void main(String[] args) throws Exception {
        //client
        getInstance().startContainer("195.148.125.214", new HashMap<>(), "johnson163/hermes-arm", 8080, "client");
        //consumer
        getInstance().startContainer("195.148.125.212", new HashMap<>(), "johnson163/hermes", 8080, "consumer");
        //producer
        getInstance().startContainer("195.148.125.213", new HashMap<>(), "johnson163/hermes-arm", 8080, "producer");
    }

    public void startContainer(String ip, Map<String, String> env, String image, int port, String type) {
        Map<String, Object> args = new HashMap<>();
        if (!type.equals("consumer")) {
            args.put("ip", ip);
        } else {
            args.put("ip", "");
        }
        args.put("env", env);
        args.put("image", image);
        args.put("port", Integer.toString(port));
        args.put("type", type);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(args);
        socket.emit("start_container", list);
    }

    public void stopContainer(String name) {

    }
}
