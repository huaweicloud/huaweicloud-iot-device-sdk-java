package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * 一个传输字符串数据的tcp server，客户端建链后，首条消息是鉴权消息，携带设备标识nodeId。
 * server将收到的消息通过gateway转发给平台
 * 用户可以扩展此类实现更复杂的TCP server
 */
public class StringTcpServer {

    private static Logger log = Logger.getLogger(StringTcpServer.class);
    private int port;
    private SimpleGateway simpleGateway;

    public StringTcpServer(int port, SimpleGateway simpleGateway) {
        this.port = port;
        this.simpleGateway = simpleGateway;
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
                }
            } else {

                //网关收到子设备上行数据时，可以以消息或者属性上报转发到平台。
                //实际使用时根据需要选择一种即可，这里为了演示，两种类型都转发一遍

                //上报消息用reportSubDeviceMessage
                DeviceMessage deviceMessage = new DeviceMessage(s);
                deviceMessage.setDeviceId(session.getDeviceId());
                simpleGateway.getIoTDevice().getClient().reportDeviceMessage(deviceMessage, null);

                //报属性则调用reportSubDeviceProperties，属性的serviceId和字段名要和子设备的产品模型保持一致
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setServiceId("smokeDetector");
                Map<String, Object> props = new HashMap<>();
                //属性值随机生成，实际中应该根据子设备上报的进行组装
                props.put("alarm", new Random().nextInt(1));
                props.put("temprature",  new Random().nextInt(100));
                serviceProperty.setProperties(props);
                DeviceProperty deviceProperty = new DeviceProperty();
                deviceProperty.setDeviceId(session.getDeviceId());
                deviceProperty.setServices(Arrays.asList(serviceProperty));

                simpleGateway.getIoTDevice().getClient().reportSubDeviceProperties(Arrays.asList(deviceProperty), null);

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
