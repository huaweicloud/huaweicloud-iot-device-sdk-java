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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个传输字符串数据的tcp server，客户端建链后，首条消息是鉴权消息，携带设备标识nodeId。server将收到的消息通过gateway转发给平台
 */
public class StringTcpServer {
    private static final Logger log = LogManager.getLogger(StringTcpServer.class);

    private static SimpleGateway simpleGateway;

    private int port;

    private StringTcpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }

        //加载iot平台的ca证书，进行服务端校验
        URL resource = StringTcpServer.class.getClassLoader().getResource("ca.jks");
        File file = new File(resource.getPath());

        simpleGateway = new SimpleGateway(new SubDevicesFilePersistence(),
            "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            "5e06bfee334dd4f33759f5b3_demo", "secret", file);

        if (simpleGateway.init() != 0) {
            return;
        }

        /*
         * 如果网关需要主动增加、删除子设备，可参考如下接口样例
         * gtwOperateSubDevices();
         */

        new StringTcpServer(port).run();

    }

    private void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializerImpl())
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

    public static class StringHandler extends SimpleChannelInboundHandler<String> {

        /**
         * @param ctx Channel处理上下文
         * @param s   Channel消息
         * @throws Exception
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
            Channel incoming = ctx.channel();
            log.info("channelRead0 is {}, the msg is {}", incoming.remoteAddress(), s);

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
                    log.info("ready to go online, the deviceId is {}", session.getDeviceId());
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
            log.error("the remote address is {}, caugh exception {}", incoming.remoteAddress(), cause.getMessage());
            // 当出现异常就关闭连接
            ctx.close();
            simpleGateway.removeSession(incoming.id().asLongText());
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

}
