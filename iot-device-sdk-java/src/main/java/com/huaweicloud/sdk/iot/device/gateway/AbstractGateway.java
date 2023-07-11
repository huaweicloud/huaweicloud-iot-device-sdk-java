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

package com.huaweicloud.sdk.iot.device.gateway;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.AddedSubDeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceStatus;
import com.huaweicloud.sdk.iot.device.gateway.requests.GtwAddSubDeviceRsp;
import com.huaweicloud.sdk.iot.device.gateway.requests.GtwDelSubDeviceRsp;
import com.huaweicloud.sdk.iot.device.gateway.requests.ScanSubdeviceNotify;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象网关，实现了子设备管理，子设备消息转发功能
 */
public abstract class AbstractGateway extends IoTDevice {
    private static final Logger log = LogManager.getLogger(AbstractGateway.class);

    private SubDevDiscoveryListener subDevDiscoveryListener;

    private final SubDevicesPersistence subDevicesPersistence;

    private GtwOperateSubDeviceListener gtwOperateSubDeviceListener;

    /**
     * 构造函数，通过设备密码认证
     *
     * @param subDevicesPersistence 子设备持久化，提供子设备信息保存能力
     * @param serverUri             平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId              设备id
     * @param deviceSecret          设备密码
     * @param file                  iot平台的ca证书，用于双向校验时设备侧校验平台
     */
    public AbstractGateway(SubDevicesPersistence subDevicesPersistence, String serverUri, String deviceId,
        String deviceSecret, File file) {
        super(serverUri, deviceId, deviceSecret, file);
        this.subDevicesPersistence = subDevicesPersistence;

        getClient().setConnectListener(new ConnectListener() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // 建连或重连时，向平台同步子设备信息
                syncSubDevices();
            }
        });
    }

    /**
     * 构造函数，通过设备证书认证
     *
     * @param subDevicesPersistence 子设备持久化，提供子设备信息保存能力
     * @param serverUri             平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId              设备id
     * @param keyStore              证书容器
     * @param keyPassword           证书密码
     * @param file                  iot平台的ca证书，用于双向校验时设备侧校验平台
     */
    public AbstractGateway(SubDevicesPersistence subDevicesPersistence, String serverUri, String deviceId,
        KeyStore keyStore, String keyPassword, File file) {
        super(serverUri, deviceId, keyStore, keyPassword, file);
        this.subDevicesPersistence = subDevicesPersistence;
        getClient().setConnectListener(new ConnectListener() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                // 建连或重连时，向平台同步子设备信息
                syncSubDevices();
            }
        });
    }

    /**
     * 设置子设备发现监听器
     *
     * @param subDevDiscoveryListener 子设备发现监听器
     */
    public void setSubDevDiscoveryListener(SubDevDiscoveryListener subDevDiscoveryListener) {
        this.subDevDiscoveryListener = subDevDiscoveryListener;
    }

    /**
     * 设置网关添加子设备监听器
     *
     * @param gtwOperateSubDeviceListener 网关操作子设备监听器
     */
    public void setGtwOperateSubDeviceListener(
        GtwOperateSubDeviceListener gtwOperateSubDeviceListener) {
        this.gtwOperateSubDeviceListener = gtwOperateSubDeviceListener;
    }

    /**
     * 根据设备标识码查询子设备
     *
     * @param nodeId 设备标识码
     * @return 子设备信息
     */
    protected DeviceInfo getSubDeviceByNodeId(String nodeId) {
        return subDevicesPersistence.getSubDevice(nodeId);
    }

    /**
     * 根据设备id查询子设备
     *
     * @param deviceId 设备id
     * @return 子设备信息
     */
    public DeviceInfo getSubDeviceByDeviceId(String deviceId) {
        String nodeId = IotUtil.getNodeIdFromDeviceId(deviceId);
        return subDevicesPersistence.getSubDevice(nodeId);
    }

    /**
     * 上报子设备发现结果
     *
     * @param deviceInfos 子设备信息列表
     * @param listener    发布监听器
     */
    public void reportSubDevList(List<DeviceInfo> deviceInfos, ActionListener listener) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("$sub_device_discovery");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("scan_result");

        Map<String, Object> para = new HashMap<>();
        para.put("devices", deviceInfos);
        deviceEvent.setParas(para);

        getClient().reportEvent(deviceEvent, listener);

    }

    /**
     * 上报子设备消息
     *
     * @param deviceMessage 设备消息
     * @param listener      监听器
     */
    public void reportSubDeviceMessage(DeviceMessage deviceMessage, ActionListener listener) {
        getClient().reportDeviceMessage(deviceMessage, listener);
    }

    /**
     * 上报子设备属性
     *
     * @param deviceId 子设备id
     * @param services 服务属性列表
     * @param listener 监听器
     */
    public void reportSubDeviceProperties(String deviceId,
        List<ServiceProperty> services,
        ActionListener listener) {

        DeviceProperty deviceProperty = new DeviceProperty();
        deviceProperty.setDeviceId(deviceId);
        deviceProperty.setServices(services);
        reportSubDeviceProperties(Collections.singletonList(deviceProperty), listener);

    }

    /**
     * 批量上报子设备属性
     *
     * @param deviceProperties 子设备属性列表
     * @param listener         发布监听器
     */
    private void reportSubDeviceProperties(List<DeviceProperty> deviceProperties,
        ActionListener listener) {

        ObjectNode node = JsonUtil.createObjectNode();
        node.putPOJO("devices", deviceProperties);

        String topic = "$oc/devices/" + getDeviceId() + "/sys/gateway/sub_devices/properties/report";

        RawMessage rawMessage = new RawMessage(topic, node.toString());

        getClient().publishRawMessage(rawMessage, listener);

    }

    /**
     * 上报子设备状态
     *
     * @param deviceId 子设备id
     * @param status   设备状态
     * @param listener 监听器
     */
    public void reportSubDeviceStatus(String deviceId, String status, ActionListener listener) {

        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setDeviceId(deviceId);
        deviceStatus.setStatus(status);

        reportSubDeviceStatus(Collections.singletonList(deviceStatus), listener);

    }

    /**
     * 批量上报子设备状态
     *
     * @param statuses 子设备状态列表
     * @param listener 发布监听器
     */
    private void reportSubDeviceStatus(List<DeviceStatus> statuses, ActionListener listener) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("sub_device_update_status");

        Map<String, Object> para = new HashMap<>();
        para.put("device_statuses", statuses);
        deviceEvent.setParas(para);
        getClient().reportEvent(deviceEvent, listener);

    }

    /**
     * 网关新增子设备请求
     *
     * @param addedSubDeviceInfos 子设备信息列表
     * @param actionListener      发布监听器
     * @param eventId             此次请求的事件Id，不携带则由平台自定生成
     */
    public void gtwAddSubDevice(List<AddedSubDeviceInfo> addedSubDeviceInfos, String eventId,
        ActionListener actionListener) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventType("add_sub_device_request");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventId(eventId);

        Map<String, Object> para = new HashMap<>();
        para.put("devices", addedSubDeviceInfos);
        deviceEvent.setParas(para);
        getClient().reportEvent(deviceEvent, actionListener);
    }

    /**
     * 网关删除子设备请求
     *
     * @param delSubDevices  要删除的子设备列表
     * @param eventId        此次请求的事件Id，不携带则由平台自定生成
     * @param actionListener 发布监听器
     */
    public void gtwDelSubDevice(List<String> delSubDevices, String eventId, ActionListener actionListener) {
        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventId(eventId);
        deviceEvent.setEventType("delete_sub_device_request");
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        Map<String, Object> para = new HashMap<>();
        para.put("devices", delSubDevices);
        deviceEvent.setParas(para);
        getClient().reportEvent(deviceEvent, actionListener);
    }

    /**
     * 事件处理回调，由SDK自动调用
     *
     * @param deviceEvents 设备事件
     */
    @Override
    public void onEvent(DeviceEvents deviceEvents) {

        super.onEvent(deviceEvents);

        for (DeviceEvent deviceEvent : deviceEvents.getServices()) {

            if ("start_scan".equals(deviceEvent.getEventType())) {

                ScanSubdeviceNotify scanSubdeviceNotify = JsonUtil.convertMap2Object(
                    deviceEvent.getParas(), ScanSubdeviceNotify.class);

                if (subDevDiscoveryListener != null) {
                    subDevDiscoveryListener.onScan(scanSubdeviceNotify);
                }

            } else if ("add_sub_device_notify".equals(deviceEvent.getEventType())) {

                SubDevicesInfo subDevicesInfo = JsonUtil.convertMap2Object(
                    deviceEvent.getParas(), SubDevicesInfo.class);

                onAddSubDevices(subDevicesInfo);

            } else if ("delete_sub_device_notify".equals(deviceEvent.getEventType())) {

                SubDevicesInfo subDevicesInfo = JsonUtil.convertMap2Object(
                    deviceEvent.getParas(), SubDevicesInfo.class);

                onDeleteSubDevices(subDevicesInfo);

            } else if ("add_sub_device_response".equals(deviceEvent.getEventType())) {

                // 跟接收子设备新增通知处理逻辑不一致
                GtwAddSubDeviceRsp gtwAddSubDeviceRsp = JsonUtil.convertMap2Object(deviceEvent.getParas(),
                    GtwAddSubDeviceRsp.class);

                if (gtwOperateSubDeviceListener != null) {
                    gtwOperateSubDeviceListener.onAddSubDeviceRsp(gtwAddSubDeviceRsp, deviceEvent.getEventId());
                }

            } else if ("delete_sub_device_response".equals(deviceEvent.getEventType())) {

                // 跟接收子设备删除通知处理逻辑不一致
                GtwDelSubDeviceRsp gtwDelSubDeviceRsp = JsonUtil.convertMap2Object(deviceEvent.getParas(),
                    GtwDelSubDeviceRsp.class);

                if (gtwOperateSubDeviceListener != null) {
                    gtwOperateSubDeviceListener.onDelSubDeviceRsp(gtwDelSubDeviceRsp, deviceEvent.getEventId());
                }
            }
        }
    }

    /**
     * 设备消息处理回调
     *
     * @param message 消息
     */
    @Override
    public void onDeviceMessage(DeviceMessage message) {

        // 子设备的
        if (message.getDeviceId() != null && !message.getDeviceId().equals(this.getDeviceId())) {
            this.onSubdevMessage(message);
        }
    }

    /**
     * 命令处理回调
     *
     * @param requestId 请求id
     * @param command   命令
     */
    @Override
    public void onCommand(String requestId, Command command) {

        // 子设备的
        if (command.getDeviceId() != null && !command.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevCommand(requestId, command);
            return;
        }

        // 网关的
        super.onCommand(requestId, command);

    }

    /**
     * 属性设置处理回调
     *
     * @param requestId 请求id
     * @param propsSet  属性设置请求
     */
    @Override
    public void onPropertiesSet(String requestId, PropsSet propsSet) {
        // 子设备的
        if (propsSet.getDeviceId() != null && !propsSet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesSet(requestId, propsSet);
            return;
        }

        // 网关的
        super.onPropertiesSet(requestId, propsSet);
    }

    /**
     * 属性查询处理回调
     *
     * @param requestId 请求id
     * @param propsGet  属性查询请求
     */
    @Override
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        // 子设备的
        if (propsGet.getDeviceId() != null && !propsGet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesGet(requestId, propsGet);
            return;
        }

        // 网关的
        super.onPropertiesGet(requestId, propsGet);
    }

    /**
     * 添加子设备处理回调，子类可以重写此接口进行扩展
     *
     * @param subDevicesInfo 子设备信息
     */
    private void onAddSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesPersistence != null) {
            subDevicesPersistence.addSubDevices(subDevicesInfo);
        }
    }

    /**
     * 删除子设备处理回调，子类可以重写此接口进行扩展
     *
     * @param subDevicesInfo 子设备信息
     * @return 处理结果，0表示成功
     */
    public int onDeleteSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesPersistence != null) {
            return subDevicesPersistence.deleteSubDevices(subDevicesInfo);
        }

        return -1;
    }

    /**
     * 向平台请求同步子设备信息
     */
    private void syncSubDevices() {
        log.info("start to syncSubDevices, local version is {}", subDevicesPersistence.getVersion());

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("sub_device_sync_request");
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        Map<String, Object> para = new HashMap<>();
        para.put("version", subDevicesPersistence.getVersion());
        deviceEvent.setParas(para);
        getClient().reportEvent(deviceEvent, null);

    }

    /**
     * 子设备命令下发处理，网关需要转发给子设备，需要子类实现
     *
     * @param requestId 请求id
     * @param command   命令
     */
    public abstract void onSubdevCommand(String requestId, Command command);

    /**
     * 子设备属性设置，网关需要转发给子设备，需要子类实现
     *
     * @param requestId 请求id
     * @param propsSet  属性设置
     */
    public abstract void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * 子设备读属性，，网关需要转发给子设备，需要子类实现
     *
     * @param requestId 请求id
     * @param propsGet  属性查询
     */
    public abstract void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     * 子设备消息下发，网关需要转发给子设备，需要子类实现
     *
     * @param message 设备消息
     */
    public abstract void onSubdevMessage(DeviceMessage message);
}
