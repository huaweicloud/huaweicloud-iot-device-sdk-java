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

import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.gateway.AbstractGateway;
import com.huaweicloud.sdk.iot.device.gateway.SubDevicesPersistence;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import io.netty.channel.Channel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此例子用来演示如何使用云网关来实现TCP协议设备接入。网关和平台只建立一个MQTT连接，使用网关的身份
 * 和平台进行通讯。本例子TCP server传输简单的字符串，并且首条消息会发送设备标识来鉴权。用户可以自行扩展StringTcpServer类
 * 来实现更复杂的TCP server。
 */
public class SimpleGateway extends AbstractGateway {
    private static final Logger log = LogManager.getLogger(SimpleGateway.class);

    private Map<String, Session> nodeIdToSesseionMap; // 保存设备标识码和session的映射

    private Map<String, Session> channelIdToSessionMap; // 保存channelId和session的映射

    SimpleGateway(SubDevicesPersistence subDevicesPersistence, String serverUri, String deviceId,
        String deviceSecret, File file) {
        super(subDevicesPersistence, serverUri, deviceId, deviceSecret, file);
        this.nodeIdToSesseionMap = new ConcurrentHashMap<>();
        this.channelIdToSessionMap = new ConcurrentHashMap<>();
    }

    Session getSessionByChannel(String channelId) {
        return channelIdToSessionMap.get(channelId);
    }

    void removeSession(String channelId) {
        Session session = channelIdToSessionMap.get(channelId);
        if (session == null) {
            return;
        }
        channelIdToSessionMap.remove(channelId);
        nodeIdToSesseionMap.remove(session.getNodeId());
        log.info("the removed session is {}", session.toString());
    }

    Session createSession(String nodeId, Channel channel) {

        // 北向已经添加了此设备
        DeviceInfo subdev = getSubDeviceByNodeId(nodeId);
        if (subdev != null) {
            Session session = new Session();
            session.setChannel(channel);
            session.setNodeId(nodeId);
            session.setDeviceId(subdev.getDeviceId());

            nodeIdToSesseionMap.put(nodeId, session);
            channelIdToSessionMap.put(channel.id().asLongText(), session);
            log.info("create new session ok, the session is {}", session.toString());
            return session;
        }

        log.info("the not allowed nodeId is {}", nodeId);
        return null;
    }

    public Session getSession(String nodeId) {
        return nodeIdToSesseionMap.get(nodeId);
    }

    @Override
    public void onSubdevMessage(DeviceMessage message) {
        if (message.getDeviceId() == null) {
            return;
        }

        String nodeId = IotUtil.getNodeIdFromDeviceId(message.getDeviceId());
        if (nodeId == null) {
            return;
        }

        Session session = nodeIdToSesseionMap.get(nodeId);
        if (session == null) {
            log.error("session is null ,nodeId:" + nodeId);
            return;
        }

        session.getChannel().writeAndFlush(message.getContent());
        log.info("writeAndFlush {}", message.getContent());

    }

    @Override
    public void onSubdevCommand(String requestId, Command command) {

        if (command.getDeviceId() == null) {
            return;
        }

        String nodeId = IotUtil.getNodeIdFromDeviceId(command.getDeviceId());
        if (nodeId == null) {
            return;
        }

        Session session = nodeIdToSesseionMap.get(nodeId);
        if (session == null) {
            log.error("session is null ,nodeId:" + nodeId);
            return;
        }

        // 这里我们直接把command对象转成string发给子设备，实际场景中可能需要进行一定的编解码转换
        session.getChannel().writeAndFlush(JsonUtil.convertObject2String(command));

        // 为了简化处理，我们在这里直接回命令响应。更合理做法是在子设备处理完后再回响应
        getClient().respondCommand(requestId, new CommandRsp(0));
        log.info("writeAndFlush command is {}", command);
    }

    @Override
    public void onSubdevPropertiesSet(String requestId, PropsSet propsSet) {

        if (propsSet.getDeviceId() == null) {
            return;
        }

        String nodeId = IotUtil.getNodeIdFromDeviceId(propsSet.getDeviceId());
        if (nodeId == null) {
            return;
        }

        Session session = nodeIdToSesseionMap.get(nodeId);
        if (session == null) {
            log.error("session is null ,nodeId:" + nodeId);
            return;
        }

        // 这里我们直接把对象转成string发给子设备，实际场景中可能需要进行一定的编解码转换
        session.getChannel().writeAndFlush(JsonUtil.convertObject2String(propsSet));

        // 为了简化处理，我们在这里直接回响应。更合理做法是在子设备处理完后再回响应
        getClient().respondPropsSet(requestId, IotResult.SUCCESS);

        log.info("writeAndFlush {}", propsSet);

    }

    @Override
    public void onSubdevPropertiesGet(String requestId, PropsGet propsGet) {

        // 不建议平台直接读子设备的属性，这里直接返回失败
        log.error("not supporte onSubdevPropertiesGet");
        getClient().respondPropsSet(requestId, IotResult.FAIL);
    }

    @Override
    public int onDeleteSubDevices(SubDevicesInfo subDevicesInfo) {

        for (DeviceInfo subdevice : subDevicesInfo.getDevices()) {
            Session session = nodeIdToSesseionMap.get(subdevice.getNodeId());
            if (session != null) {
                if (session.getChannel() != null) {
                    session.getChannel().close();
                    channelIdToSessionMap.remove(session.getChannel().id().asLongText());
                    nodeIdToSesseionMap.remove(session.getNodeId());
                }
            }
        }
        return super.onDeleteSubDevices(subDevicesInfo);

    }

}
