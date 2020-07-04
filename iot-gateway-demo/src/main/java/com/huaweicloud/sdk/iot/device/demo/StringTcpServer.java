package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 一个传输字符串数据的tcp server，客户端建链后，首条消息是鉴权消息，携带设备标识nodeId。server将收到的消息通过gateway转发给平台
 */
public class StringTcpServer {

    private static SimpleGateway simpleGateway;
    private static Logger log = Logger.getLogger(StringTcpServer.class);
    private int port;

    public StringTcpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }

        simpleGateway = new SimpleGateway(new SubDevicesFilePersistence(),
                "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5ebac693352cfb02c567ec88_abc123455", "cdd22e27c1447a6c694ee56a0b869218");
        if (simpleGateway.init() != 0) {
            return;
        }
        Logger.getLogger("io.netty").setLevel(Level.INFO);
        new StringTcpServer(port).run();

    }

    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("decoder", new StringDecoder());
                            ch.pipeline().addLast("encoder", new StringEncoder());
                            ch.pipeline().addLast("handler", new StringHandler());

                            log.info("initChannel:" + ch.remoteAddress());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
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

    public class StringHandler extends SimpleChannelInboundHandler<String> {


        /**
         * @param ctx
         * @param s
         * @throws Exception
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Channel incoming = ctx.channel();
            log.info("channelRead0" + incoming.remoteAddress() + " msg :" + s);

            //如果是首条消息,创建session
            Session session = simpleGateway.getSessionByChannel(incoming.id().asLongText());
            if (session == null) {
                String nodeId = s;
                session = simpleGateway.createSession(nodeId, incoming);

                //创建会话失败，拒绝连接
                if (session == null) {
                    log.info("close channel");
                    ctx.close();
                } else {
                    log.info(session.getDeviceId() + " ready to go online.");
                    simpleGateway.reportSubDeviceStatus(session.getDeviceId(), "ONLINE", null);
                }

            } else {

                //网关收到子设备上行数据时，可以以消息或者属性上报转发到平台。
                //实际使用时根据需要选择一种即可，这里为了演示，两种类型都转发一遍

                //上报消息用reportSubDeviceMessage
                DeviceMessage deviceMessage = new DeviceMessage(s);
                deviceMessage.setDeviceId(session.getDeviceId());
                simpleGateway.reportSubDeviceMessage(deviceMessage, null);

                //报属性则调用reportSubDeviceProperties，属性的serviceId和字段名要和子设备的产品模型保持一致
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setServiceId("parameter");
                Map<String, Object> props = new HashMap<>();
                //属性值暂且写死，实际中应该根据子设备上报的进行组装
                props.put("alarm", 1);
                props.put("temprature", 2);
                serviceProperty.setProperties(props);
                simpleGateway.reportSubDeviceProperties(session.getDeviceId(), Arrays.asList(serviceProperty), null);

            }

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Channel incoming = ctx.channel();
            log.info("exceptionCaught:" + incoming.remoteAddress());
            // 当出现异常就关闭连接
            log.error(cause);
            ctx.close();
            simpleGateway.removeSession(incoming.id().asLongText());
        }
    }

}
