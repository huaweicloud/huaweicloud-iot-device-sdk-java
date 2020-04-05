package com.huaweicloud.sdk.iot.device.gateway;


import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;

/**
 * 提供子设备信息持久化保存
 */
public interface SubDevicesPersistence {

    DeviceInfo getSubDevice(String nodeId);

    int addSubDevices(SubDevicesInfo subDevicesInfo);

    int deleteSubDevices(SubDevicesInfo subDevicesInfo);

    long getVersion();

}
