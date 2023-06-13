package com.huaweicloud.bridge.sdk.listener;

import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;

import java.util.List;

/**
 * 属性监听器，用于接收平台下发的属性读写操作
 */
public interface BridgePropertyListener {

    /**
     * 处理写属性操作
     *
     * @param requestId 设备id
     * @param requestId 请求id
     * @param services  服务属性列表
     */
    void onPropertiesSet(String deviceId, String requestId, List<ServiceProperty> services);

    /**
     * 处理读属性操作
     *
     * @param requestId 设备id
     * @param requestId 请求id
     * @param serviceId 服务id，可选
     */
    void onPropertiesGet(String deviceId, String requestId, String serviceId);
}
