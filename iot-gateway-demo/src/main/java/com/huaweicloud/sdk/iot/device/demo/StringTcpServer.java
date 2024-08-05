/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.gateway.GtwOperateSubDeviceListener;
import com.huaweicloud.sdk.iot.device.gateway.requests.AddedSubDeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.GtwAddSubDeviceRsp;
import com.huaweicloud.sdk.iot.device.gateway.requests.GtwDelSubDeviceRsp;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * 一个传输字符串数据的tcp server，客户端建链后，首条消息是鉴权消息，携带设备标识nodeId。server将收到的消息通过gateway转发给平台
 */
public class StringTcpServer {
    private static final Logger log = LogManager.getLogger(StringTcpServer.class);

    private static SimpleGateway simpleGateway;

    private final int port;

    private StringTcpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int serverPort;
        if (args.length > 0) {
            serverPort = Integer.parseInt(args[0]);
        } else {
            serverPort = 8080;
        }

        // 加载iot平台的ca证书，进行服务端校验
        URL resource = StringTcpServer.class.getClassLoader().getResource("ca.jks");
        File file = new File(resource.getPath());

        // 用户请替换为自己的接入地址。
        simpleGateway = new SimpleGateway(new SubDevicesFilePersistence(),
            "ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883",
            "5e06bfee334dd4f33759f5b3_demo", "secret", file);

        simpleGateway.setGtwOperateSubDeviceListener(new GtwOperateSubDeviceListener() {
            @Override
            public void onAddSubDeviceRsp(GtwAddSubDeviceRsp gtwAddSubDeviceRsp, String eventId) {
                log.info("add device result={}", gtwAddSubDeviceRsp);
            }

            @Override
            public void onDelSubDeviceRsp(GtwDelSubDeviceRsp gtwDelSubDeviceRsp, String eventId) {
                log.info("delete result={}", gtwDelSubDeviceRsp);
            }
        });

        if (simpleGateway.init() != 0) {
            return;
        }

        // 如果网关需要主动增加、删除子设备，可参考如下接口样例
        gtwOperateSubDevices();

        new StringTcpServer(serverPort).run();
    }

    private static boolean addSubDevices(String[] lines) {
        if (lines.length != 3) {
            log.warn("please input add [productId] [nodeId] or delete [deviceId]");
            return false;
        }
        List<AddedSubDeviceInfo> addedSubDeviceInfos = new ArrayList<>();
        AddedSubDeviceInfo addedSubDeviceInfo = new AddedSubDeviceInfo();
        addedSubDeviceInfo.setProductId(lines[1]);
        addedSubDeviceInfo.setNodeId(lines[2]);
        addedSubDeviceInfos.add(addedSubDeviceInfo);
        simpleGateway.gtwAddSubDevice(addedSubDeviceInfos, null, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("gtwAddSubDevice success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("gtwAddSubDevice failed, nodeId={}, error={}", lines[1],
                        ExceptionUtil.getBriefStackTrace(var2));
            }
        });
        return true;
    }

    private static boolean deleteSubDevices(String[] lines) {
        if (lines.length != 2) {
            log.warn("please input add [productId] [nodeId] or delete [deviceId]");
            return false;
        }
        List<String> delSubDevices = new ArrayList<>();
        delSubDevices.add(lines[1]);
        simpleGateway.gtwDelSubDevice(delSubDevices, null, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("gtwDelSubDevice success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("gtwDelSubDevice failed, deviceId={}, error={}", lines[1],
                        ExceptionUtil.getBriefStackTrace(var2));
            }
        });
        return true;
    }

    private static void setSubDevice(BufferedReader in) throws IOException {
        String line = in.readLine();
        final String[] s = line.split("\\s+");
        // add productId nodeId
        if ("add".equals(s[0])) {
            addSubDevices(s);
        } else if ("delete".equals(s[0])) {  // delete deviceId
            deleteSubDevices(s);
        } else {
            log.warn("please input add [productId] [nodeId] or delete [deviceId]");
        }
    }

    public static void gtwOperateSubDevices() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                    while (true) {
                        setSubDevice(in);
                    }
                } catch (Exception e) {
                    log.error("gtwOperateSubDevices failed" + e.getMessage());
                }
            }
        }).start();
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

            // 如果是首条消息,创建session
            Session session = simpleGateway.getSessionByChannel(incoming.id().asLongText());
            if (session == null) {
                String nodeId = s;
                session = simpleGateway.createSession(nodeId, incoming);

                // 创建会话失败，拒绝连接
                if (session == null) {
                    log.info("close channel");
                    ctx.close();
                } else {
                    log.info("ready to go online, the deviceId is {}", session.getDeviceId());
                    simpleGateway.reportSubDeviceStatus(session.getDeviceId(), "ONLINE", null);
                }

            } else {

                // 网关收到子设备上行数据时，可以以消息或者属性上报转发到平台。
                // 实际使用时根据需要选择一种即可，这里为了演示，两种类型都转发一遍

                // 上报消息用reportSubDeviceMessage
                DeviceMessage deviceMessage = new DeviceMessage(s);
                deviceMessage.setDeviceId(session.getDeviceId());
                simpleGateway.reportSubDeviceMessage(deviceMessage, null);

                // 报属性则调用reportSubDeviceProperties，属性的serviceId和字段名要和子设备的产品模型保持一致
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setServiceId("parameter");
                Map<String, Object> props = new HashMap<>();
                // 属性值暂且写死，实际中应该根据子设备上报的进行组装
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
