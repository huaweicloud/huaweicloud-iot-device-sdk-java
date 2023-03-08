package com.huaweicloud.sdk.iot.device.gateway;

import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;

/**
 * 提供子设备信息持久化保存，网关场景使用
 */
public interface SubDevicesPersistence {
    /**
     * 获取子设备信息
     *
     * @param nodeId 设备标识码
     * @return 子设备信息
     */
    DeviceInfo getSubDevice(String nodeId);

    /**
     * 添加子设备接口
     *
     * @param subDevicesInfo 子设备信息
     * @return 0代表成功；其它代表失败
     */
    int addSubDevices(SubDevicesInfo subDevicesInfo);

    /**
     * 删除子设备接口
     *
     * @param subDevicesInfo 子设备信息
     * @return 0代表成功；其它代表失败
     */
    int deleteSubDevices(SubDevicesInfo subDevicesInfo);

    /**
     * 获取设备版本号
     *
     * @return 设备版本号
     */
    long getVersion();

}
