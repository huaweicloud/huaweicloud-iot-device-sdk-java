package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 * 一个tcp客户端，模拟一个tcp设备，仅用于测试。注意设备连到server后，必选首先发送鉴权消息，
 * 携带设备标识码
 */
public class TcpDevice {

    private final String host;
    private final int port;
    private Logger log = Logger.getLogger(TcpDevice.class);

    public TcpDevice(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        Logger.getLogger("io.netty").setLevel(Level.INFO);
        new TcpDevice("localhost", 8080).run();
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))){
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
            log.error(ExceptionUtil.getBriefStackTrace(e));
        } finally {
            group.shutdownGracefully();
        }

    }

    public class SimpleClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            log.info("channelRead0:" + s);
        }
    }

    public class SimpleClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            log.info("initChannel...");

            //pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("handler", new SimpleClientHandler());
        }
    }
}
