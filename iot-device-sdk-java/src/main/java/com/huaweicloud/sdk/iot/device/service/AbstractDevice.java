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

package com.huaweicloud.sdk.iot.device.service;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.constants.Constants;
import com.huaweicloud.sdk.iot.device.devicelog.DeviceLogService;
import com.huaweicloud.sdk.iot.device.devicelog.listener.DefaultConnActionLogListener;
import com.huaweicloud.sdk.iot.device.devicelog.listener.DefaultConnLogListener;
import com.huaweicloud.sdk.iot.device.filemanager.FileManagerService;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.timesync.TimeSyncService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象设备类
 */
public class AbstractDevice {
    private static final Logger log = LogManager.getLogger(AbstractService.class);

    private DeviceClient client;

    private final String deviceId;

    private final Map<String, AbstractService> services = new ConcurrentHashMap<>();

    private OTAService otaService;

    private FileManagerService fileManagerService;

    private TimeSyncService timeSyncService;

    private DeviceLogService deviceLogService;

    /**
     * 构造函数，使用密码创建设备
     *
     * @param serverUri    平台访问地址，比如ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     * @param iotCertFile  iot平台的ca证书，用于双向校验时设备侧校验平台
     */
    public AbstractDevice(String serverUri, String deviceId, String deviceSecret, File iotCertFile) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(serverUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setSecret(deviceSecret);
        clientConf.setFile(iotCertFile);
        this.deviceId = deviceId;
        this.client = new DeviceClient(clientConf, this);
        initSysServices();
        log.info("create device, the deviceId is {}", clientConf.getDeviceId());

    }

    /**
     * 构造函数，使用证书创建设备
     *
     * @param serverUri   平台访问地址，比如ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId    设备id
     * @param keyStore    证书容器
     * @param keyPassword 证书密码
     * @param iotCertFile iot平台的ca证书，用于双向校验时设备侧校验平台
     */
    public AbstractDevice(String serverUri, String deviceId, KeyStore keyStore, String keyPassword, File iotCertFile) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(serverUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setKeyPassword(keyPassword);
        clientConf.setKeyStore(keyStore);
        clientConf.setFile(iotCertFile);
        this.deviceId = deviceId;
        this.client = new DeviceClient(clientConf, this);
        initSysServices();
        log.info("create device {} ", clientConf.getDeviceId());
    }

    /**
     * 构造函数，直接使用客户端配置创建设备
     *
     * @param clientConf 客户端配置
     */
    public AbstractDevice(ClientConf clientConf) {
        if (clientConf.getMode() == Constants.CONNECT_OF_NORMAL_DEVICE_MODE) {
            this.client = new DeviceClient(clientConf, this);
        }

        this.deviceId = clientConf.getDeviceId();
        initSysServices();
        log.info("create device: {}", clientConf.getDeviceId());
    }

    /**
     * 初始化系统默认service，系统service以$作为开头
     */
    private void initSysServices() {
        this.otaService = new OTAService();
        this.addService("$ota", otaService);
        this.fileManagerService = new FileManagerService();
        this.addService("$file_manager", fileManagerService);
        this.addService("$sdk", new SdkInfo());

        this.timeSyncService = new TimeSyncService();
        this.addService("$time_sync", timeSyncService);

        this.deviceLogService = new DeviceLogService();
        this.addService("$log", deviceLogService);
    }

    /**
     * 初始化，创建到平台的连接
     *
     * @return 如果连接成功，返回0；其它表示失败
     */
    public int init() {

        // 如果日志上报开关是关闭状态或者已过日志收集结束时间，则取消上报设备连接状态相关日志
        if (deviceLogService.canReportLog()) {
            DefaultConnLogListener defaultConnLogListener = new DefaultConnLogListener(deviceLogService);
            client.setConnectListener(defaultConnLogListener);

            DefaultConnActionLogListener defaultConnActionLogListener = new DefaultConnActionLogListener(
                deviceLogService);
            client.setConnectActionListener(defaultConnActionLogListener);
        }

        return client.connect();
    }

    /**
     * 添加服务。用户基于AbstractService定义自己的设备服务，并添加到设备
     *
     * @param serviceId     服务id，要和设备模型定义一致
     * @param deviceService 服务实例
     */
    public void addService(String serviceId, AbstractService deviceService) {

        deviceService.setIotDevice(this);
        deviceService.setServiceId(serviceId);

        services.putIfAbsent(serviceId, deviceService);
    }

    /**
     * 删除服务
     *
     * @param serviceId 服务id
     */
    public void delService(String serviceId) {
        services.remove(serviceId);
    }

    /**
     * 查询服务
     *
     * @param serviceId 服务id
     * @return AbstractService 服务实例
     */
    public AbstractService getService(String serviceId) {

        return services.get(serviceId);
    }

    /**
     * 触发属性变化，SDK会上报变化的属性
     *
     * @param serviceId  服务id
     * @param properties 属性列表
     */
    protected void firePropertiesChanged(String serviceId, String... properties) {
        AbstractService deviceService = getService(serviceId);
        if (deviceService == null) {
            return;
        }
        Map props = deviceService.onRead(properties);

        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId(deviceService.getServiceId());
        serviceProperty.setProperties(props);
        serviceProperty.setEventTime(IotUtil.getTimeStamp());

        getClient().scheduleTask(new Runnable() {
            @Override
            public void run() {
                client.reportProperties(Arrays.asList(serviceProperty), new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("reportProperties failed: " + var2.toString());
                    }
                });
            }
        });

    }

    /**
     * 触发多个服务的属性变化，SDK自动上报变化的属性到平台
     *
     * @param serviceIds 发生变化的服务id列表
     */
    protected void fireServicesChanged(List<String> serviceIds) {
        List<ServiceProperty> serviceProperties = new ArrayList<>();
        for (String serviceId : serviceIds) {
            AbstractService deviceService = getService(serviceId);
            if (deviceService == null) {
                log.error("service not found: " + serviceId);
                continue;
            }

            Map props = deviceService.onRead();

            ServiceProperty serviceProperty = new ServiceProperty();
            serviceProperty.setServiceId(deviceService.getServiceId());
            serviceProperty.setProperties(props);
            serviceProperty.setEventTime(IotUtil.getTimeStamp());
            serviceProperties.add(serviceProperty);
        }

        if (serviceProperties.isEmpty()) {
            return;
        }

        getClient().scheduleTask(new Runnable() {
            @Override
            public void run() {
                client.reportProperties(serviceProperties, new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("reportProperties failed: " + var2.toString());
                    }
                });
            }
        });
    }

    /**
     * 获取设备客户端。获取到设备客户端后，可以直接调用客户端提供的消息、属性、命令等接口
     *
     * @return 设备客户端实例
     */
    public DeviceClient getClient() {
        return client;
    }

    /**
     * 查询设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 命令回调函数，由SDK自动调用
     *
     * @param requestId 请求id
     * @param command   命令
     */
    public void onCommand(String requestId, Command command) {

        IService service = getService(command.getServiceId());

        if (service != null) {
            CommandRsp rsp = service.onCommand(command);
            client.respondCommand(requestId, rsp);
        }

    }

    /**
     * 属性设置回调，，由SDK自动调用
     *
     * @param requestId 请求id
     * @param propsSet  属性设置请求
     */
    public void onPropertiesSet(String requestId, PropsSet propsSet) {

        for (ServiceProperty serviceProp : propsSet.getServices()) {
            IService deviceService = getService(serviceProp.getServiceId());

            if (deviceService != null) {

                // 如果部分失败直接返回
                IotResult result = deviceService.onWrite(serviceProp.getProperties());
                if (result.getResultCode() != IotResult.SUCCESS.getResultCode()) {
                    client.respondPropsSet(requestId, result);
                    return;
                }
            }
        }
        client.respondPropsSet(requestId, IotResult.SUCCESS);

    }

    /**
     * 属性查询回调，由SDK自动调用
     *
     * @param requestId 请求id
     * @param propsGet  属性查询请求
     */
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        List<ServiceProperty> serviceProperties = new ArrayList<>();

        // 查询所有
        if (propsGet.getServiceId() == null) {

            for (String ss : services.keySet()) {
                IService deviceService = getService(ss);
                if (deviceService != null) {
                    Map properties = deviceService.onRead();
                    ServiceProperty serviceProperty = new ServiceProperty();
                    serviceProperty.setProperties(properties);
                    serviceProperty.setServiceId(ss);
                    serviceProperties.add(serviceProperty);
                }
            }
        } else {
            IService deviceService = getService(propsGet.getServiceId());

            if (deviceService != null) {
                Map properties = deviceService.onRead();
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setProperties(properties);
                serviceProperty.setServiceId(propsGet.getServiceId());
                serviceProperties.add(serviceProperty);

            }
        }

        client.respondPropsGet(requestId, serviceProperties);

    }

    /**
     * 事件回调，由SDK自动调用
     *
     * @param deviceEvents 设备事件
     */
    public void onEvent(DeviceEvents deviceEvents) {

        // 子设备的
        if (deviceEvents.getDeviceId() != null && !deviceEvents.getDeviceId().equals(getDeviceId())) {
            return;
        }

        for (DeviceEvent event : deviceEvents.getServices()) {
            IService deviceService = getService(event.getServiceId());
            if (deviceService != null) {
                deviceService.onEvent(event);
            }
        }
    }

    /**
     * 事件回调，网桥场景下使用
     *
     * @param deviceId     设备Id
     * @param deviceEvents 设备事件内容
     */
    public void onBridgeEvent(String deviceId, DeviceEvents deviceEvents) {
        for (DeviceEvent event : deviceEvents.getServices()) {
            IService deviceService = getService(event.getServiceId());
            if (deviceService != null) {
                deviceService.onBridgeEvent(deviceId, event);
            }
        }
    }

    /**
     * 消息回调，由SDK自动调用
     *
     * @param message 消息
     */
    public void onDeviceMessage(DeviceMessage message) {

    }

    /**
     * 获取OTA服务
     *
     * @return OTAService
     */
    public OTAService getOtaService() {
        return otaService;
    }

    /**
     * 获取时间同步服务
     *
     * @return
     */
    public TimeSyncService getTimeSyncService() {
        return timeSyncService;
    }

    public DeviceLogService getDeviceLogService() {
        return deviceLogService;
    }

    public FileManagerService getFileManagerService() {
        return fileManagerService;
    }

    public void setFileManagerService(FileManagerService fileManagerService) {
        this.fileManagerService = fileManagerService;
    }
}
