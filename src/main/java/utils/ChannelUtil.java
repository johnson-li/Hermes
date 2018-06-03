package utils;

import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ChannelUtil {
    private static Logger logger = LoggerFactory.getLogger(ChannelUtil.class);
    private static ChannelUtil instance = new ChannelUtil();
    private EventLoopGroup ioGroup = new NioEventLoopGroup();
    private EventLoopGroup workingGroup = new NioEventLoopGroup();
    private Map<String, ManagedChannel> cache = new HashMap<>();

    public static ChannelUtil getInstance() {
        return instance;
    }

    public void terminate() {
        ioGroup.shutdownGracefully();
        workingGroup.shutdownGracefully();
    }

    public EventLoopGroup getWorkingGroup() {
        return workingGroup;
    }

    public void execute(Runnable runnable) {
        getWorkingGroup().execute(runnable);
    }

    public void execute(Runnable runnable, long delay) {
        workingGroup.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public ManagedChannel getClientChannel(String ip, int port) throws SSLException {
        String key = String.format("%s:%d", ip, port);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        SslContextBuilder sslContextBuilder =
                GrpcSslContexts.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE);
        SslContext sslContext = sslContextBuilder.build();
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(ip, port)
                .eventLoopGroup(ioGroup)
                .channelType(NioSocketChannel.class).negotiationType(NegotiationType.TLS).sslContext(sslContext);
        ManagedChannel channel = builder.build();
        cache.put(key, channel);
        return channel;
    }
}
