import io.socket.client.IO;
import io.socket.client.Socket;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws Exception {
        int cube = 7;
        IntStream.range(0, cube).forEach(i -> System.out.println(String.format("double8_t x%d = compressed[nxx * (i * cube + %d) + k];", i, i)));
        IntStream.range(0, cube).forEach(i -> System.out.println(String.format("double8_t y%d = compressed[nxx * (j * cube + %d) + k];", i, i)));
        IntStream.range(0, cube).forEach(i -> IntStream.range(0, cube).forEach(j -> System.out.println(String.format("vv[%d][%d] += x%d * y%d;", i, j, i, j))));
    }

    static void test() throws Exception {
        Socket socket = IO.socket("http://localhost:3000");
        socket.on("cpu_stats", args1 -> Arrays.stream(args1).forEach(a -> System.out.println(a.getClass())));
        socket.connect();
    }
}
