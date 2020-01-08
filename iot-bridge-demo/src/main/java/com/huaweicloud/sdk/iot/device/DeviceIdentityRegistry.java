package com.huaweicloud.sdk.iot.device;


/**
 * 设备接入标识管理，提供获取设备标识信息能力
 */
public interface DeviceIdentityRegistry {

    DeviceIdentity getDeviceIdentity(String nodeId);

}
