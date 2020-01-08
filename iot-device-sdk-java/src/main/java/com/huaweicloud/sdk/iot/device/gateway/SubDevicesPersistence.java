package com.huaweicloud.sdk.iot.device.gateway;


import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;

/**
 * 提供子设备信息持久化保存
 */
public interface SubDevicesPersistence {

    public DeviceInfo getSubDevice(String nodeId);

    public int addSubDevices(SubDevicesInfo subDevicesInfo);

    public int deleteSubDevices(SubDevicesInfo subDevicesInfo);

}
