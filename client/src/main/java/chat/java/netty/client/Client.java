package chat.java.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        public void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("handler", new ClientHandler());
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            System.out.println("Connected to server on " + host + ":" + port + ". To exit write: 'bye'");

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String message = in.readLine();
                if (message == null || message.equalsIgnoreCase("bye")) {
                    break;
                }

                f.channel().writeAndFlush(message);
            }

        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8888;
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        new Client(host, port).run();
    }
}
