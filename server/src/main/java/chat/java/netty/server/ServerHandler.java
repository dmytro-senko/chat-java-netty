package chat.java.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;


public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final List<ChannelHandlerContext> clients = new ArrayList<>();
    private String namePlayer;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx);
        namePlayer = ctx.channel().remoteAddress().toString().split(":")[1];
        String message = "New Player " + namePlayer + " joined the chat" ;
        broadcastMessage(message);
        System.out.println(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.equalsIgnoreCase("bye")) {
            channelInactive(ctx);
        }
        String message = "[Player " + namePlayer + "] " + msg;
        System.out.println(message);
        for (ChannelHandlerContext client : clients) {
            if (client != ctx) {
                client.writeAndFlush(message);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clients.remove(ctx);
        String message = "Player " + namePlayer + " left the chat!";
        broadcastMessage(message);
        System.out.println(message);
        ctx.close();
    }

    private void broadcastMessage(String message) {
        for (ChannelHandlerContext client : clients) {
            client.writeAndFlush(message);
        }
    }
}