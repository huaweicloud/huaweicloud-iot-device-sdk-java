package com.huaweicloud.sdk.iot.device.gateway;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceStatus;
import com.huaweicloud.sdk.iot.device.gateway.requests.ScanSubdeviceNotify;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象网关，实现了子设备管理，子设备消息转发功能
 */
public abstract class AbstractGateway extends IoTDevice {

    private static final Logger log = Logger.getLogger(AbstractGateway.class);

    private SubDevDiscoveryListener subDevDiscoveryListener;
    private SubDevicesPersistence subDevicesPersistence;

    public AbstractGateway(SubDevicesPersistence subDevicesPersistence, ClientConf clientConf) {
        super(clientConf);
        this.subDevicesPersistence = subDevicesPersistence;
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
     * 根据设备标识码查询子设备
     *
     * @param nodeId 设备标识码
     * @return 子设备信息
     */
    public DeviceInfo getSubDeviceByNodeId(String nodeId) {
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
        deviceEvent.setServiceId("sub_device_discovery");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("scan_result");

        Map<String, Object> para = new HashMap<>();
        para.put("devices", deviceInfos);
        deviceEvent.setParas(para);

        getClient().reportEvent(deviceEvent, listener);

    }

    /**
     * 发布子设备消息
     *
     * @param deviceMessage 设备消息
     * @param listener      监听器
     */
    public void publishSubDeviceMessage(DeviceMessage deviceMessage, ActionListener listener) {
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
        reportSubDeviceProperties(Arrays.asList(deviceProperty), listener);

    }

    /**
     * 批量上报子设备属性
     *
     * @param deviceProperties 子设备属性列表
     * @param listener         发布监听器
     */
    public void reportSubDeviceProperties(List<DeviceProperty> deviceProperties,
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

        reportSubDeviceStatus(Arrays.asList(deviceStatus), listener);

    }


    /**
     * 批量上报子设备状态
     *
     * @param statuses 子设备状态列表
     * @param listener 发布监听器
     */
    public void reportSubDeviceStatus(List<DeviceStatus> statuses, ActionListener listener) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("sub_device_update_status");

        Map<String, Object> para = new HashMap<>();
        para.put("device_statuses", statuses);
        deviceEvent.setParas(para);
        getClient().reportEvent(deviceEvent, listener);

    }

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

            }
        }
    }

    @Override
    public void onDeviceMessage(DeviceMessage message) {

        //子设备的
        if (message.getDeviceId() != null && !message.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevMessage(message);
            return;
        }
    }

    @Override
    public void onCommand(String requestId, Command command) {

        //子设备的
        if (command.getDeviceId() != null && !command.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevCommand(requestId, command);
            return;
        }

        //网关的
        super.onCommand(requestId, command);

    }

    @Override
    public void onPropertiesSet(String requestId, PropsSet propsSet) {
        //子设备的
        if (propsSet.getDeviceId() != null && !propsSet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesSet(requestId, propsSet);
            return;
        }

        //网关的
        super.onPropertiesSet(requestId, propsSet);

    }

    @Override
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        //子设备的
        if (propsGet.getDeviceId() != null && !propsGet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesGet(requestId, propsGet);
            return;
        }

        //网关的
        super.onPropertiesGet(requestId, propsGet);
    }

    public int onAddSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesPersistence != null) {
            return subDevicesPersistence.addSubDevices(subDevicesInfo);
        }
        return -1;
    }

    public int onDeleteSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesPersistence != null) {
            return subDevicesPersistence.deleteSubDevices(subDevicesInfo);
        }
        return -1;
    }


    /**
     * 子设备命令下发通知
     *
     * @param requestId 请求id
     * @param command   命令
     */
    public abstract void onSubdevCommand(String requestId, Command command);

    /**
     * 子设备属性设置通知
     *
     * @param requestId
     * @param propsSet
     */
    public abstract void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * 子设备读属性通知
     *
     * @param requestId
     * @param propsGet
     */
    public abstract void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     * 子设备消息下发
     *
     * @param message
     */
    public abstract void onSubdevMessage(DeviceMessage message);
}
