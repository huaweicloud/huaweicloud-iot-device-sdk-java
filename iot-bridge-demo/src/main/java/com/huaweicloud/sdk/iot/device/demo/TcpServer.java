package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 一个传输字符串数据的tcp server，客户端建链后，首条消息是鉴权消息，携带设备标识nodeId。server将收到的消息通过bridge转发给平台
 */
public class TcpServer {

    private static final Logger log = LogManager.getLogger(TcpServer.class);

    private int port;

    TcpServer(int port) {
        this.port = port;
    }

    void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializerImpl())
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            log.info("tcp server start......");

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            log.info("tcp server close");
        }
    }

    private static class ChannelInitializerImpl extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast("decoder", new StringDecoder());
            ch.pipeline().addLast("encoder", new StringEncoder());
            ch.pipeline().addLast("handler", new StringHandler());

            log.info("initChannel: {}", ch.remoteAddress());
        }
    }

    public static class StringHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Channel incoming = ctx.channel();
            log.info("channelRead0: {}, msg : {}", incoming.remoteAddress(), s);

            // 如果是首条消息,创建session
            Session session = Bridge.getInstance().getSessionByChannel(incoming.id().asLongText());
            if (session == null) {
                Bridge.getInstance().createSession(s, incoming);
            } else {
                session.getDeviceClient().reportDeviceMessage(new DeviceMessage(s), null);
            }

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel incoming = ctx.channel();
            log.error("the remote address is {} , caught exception : {}", incoming.remoteAddress(), cause.getMessage());

            // 当出现异常就关闭连接
            ctx.close();
            Bridge.getInstance().removeSession(incoming.id().asLongText());
        }
    }

}
