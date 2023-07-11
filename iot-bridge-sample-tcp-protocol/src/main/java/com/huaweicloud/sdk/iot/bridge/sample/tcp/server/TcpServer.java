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

package com.huaweicloud.sdk.iot.bridge.sample.tcp.server;

import com.huaweicloud.sdk.iot.bridge.sample.tcp.codec.MessageDecoder;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.codec.MessageEncoder;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.handler.UpLinkHandler;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 模拟定时进行位置上报的电子学生卡服务端，设备的协议数据如下：
 * <p>
 * 1、客户端同平台建链连接后，发起登录请求，格式如下：
 * [867082058798193,0,DEVICE_LOGIN,3,12345678]
 * 2、平台鉴权通过后，给设备返回登录成功的响应，格式如下：
 * [867082058798193,0,DEVICE_LOGIN,4,0]
 * <p>
 * 3、设备鉴权成功后，定时上报位置信息，格式如下：
 * [867082058798193,1,REPORT_LOCATION_INFO,3,116.307629@40.058359]
 * 4、平台返回位置上报的响应
 * [867082058798193,1,REPORT_LOCATION_INFO,4,0]
 * <p>
 * 5、平台下发设置位置上报的周期：
 * [867082058798193,2,FREQUENCY_LOCATION_SET,1,5]
 * 5、设备返回设置设备上报周期的响应：
 * [867082058798193,2,FREQUENCY_LOCATION_SET,2,0]
 */
public class TcpServer {

    private static final Logger log = LogManager.getLogger(TcpServer.class);

    private static final int MAX_FRAME_LENGTH = 1024;

    private static final int DEFAULT_BUF_VALUE = 1024 * 1024;

    private static final int DEFAULT_IDLE_TIME = 300;

    private static final char DELIMITER_CHAR = ']';

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;

    private final Class<? extends ServerSocketChannel> channelClass;

    public TcpServer() {
        if (Epoll.isAvailable()) {
            log.info("Netty is using Epoll");
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        } else {
            log.info("Netty is using NIO");
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            channelClass = NioServerSocketChannel.class;
        }
    }

    public void start(String host, int port) {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(channelClass)
                .childHandler(new ServerChannelHandler())
                .option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.SO_RCVBUF, DEFAULT_BUF_VALUE)
                .childOption(ChannelOption.SO_SNDBUF, DEFAULT_BUF_VALUE);
            ChannelFuture f = bootstrap.bind(host, port);
            f.sync();
            log.info("Binding server. host={}, port={}", host, port);
        } catch (Exception e) {
            log.warn("Binding server host={}, port={} exception {}", host, port,
                ExceptionUtil.getBriefStackTrace(e));
        }
    }

    static class ServerChannelHandler extends ChannelInitializer {

        @Override
        protected void initChannel(Channel channel) {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addFirst("readTimeoutHandler",
                new ReadTimeoutHandler(DEFAULT_IDLE_TIME));
            pipeline.addLast("delimiterDecoder", new DelimiterBasedFrameDecoder(MAX_FRAME_LENGTH,
                Unpooled.copiedBuffer(new byte[] {DELIMITER_CHAR})));
            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("messageDecoder", new MessageDecoder());
            pipeline.addLast("encoder", new StringEncoder());
            pipeline.addLast("messageEncoder", new MessageEncoder());
            pipeline.addLast("handler", new UpLinkHandler());
        }
    }
}
