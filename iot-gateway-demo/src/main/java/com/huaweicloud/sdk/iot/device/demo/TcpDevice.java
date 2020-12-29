package com.huaweicloud.sdk.iot.device.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * 一个tcp客户端，仅用于测试
 */
public class TcpDevice {

    private final String host;
    private final int port;
    private static final Logger log = LogManager.getLogger(TcpDevice.class);

    private TcpDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {
        new TcpDevice("localhost", 8080).run();
    }

    private void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new SimpleClientInitializer());
            Channel channel = bootstrap.connect(host, port).sync().channel();

            while (true) {
                log.info("input string to send:");
                channel.writeAndFlush(in.readLine());
            }
        } catch (Exception e) {
            log.error("run task failed" + e.getMessage());
            group.shutdownGracefully();
        }
    }

    public class SimpleClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) {
            log.info("channelRead0:" + s);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.info("exceptionCaught " + cause.toString());
            ctx.close();
        }
    }

    public class SimpleClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            log.info("initChannel...");

            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("handler", new SimpleClientHandler());
        }
    }
}
