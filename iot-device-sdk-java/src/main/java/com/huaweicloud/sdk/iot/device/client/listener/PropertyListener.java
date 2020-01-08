package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;

import java.util.List;

/**
 * 属性监听器，用于接收平台下发的属性读写操作
 */
public interface PropertyListener {


    /**
     * 处理写属性操作
     *
     * @param requestId 请求id
     * @param services  服务属性列表
     */
    public void onPropertiesSet(String requestId, List<ServiceProperty> services);

    /**
     * 处理读属性操作
     *
     * @param requestId 请求id
     * @param serviceId 服务id，可选
     */
    public void onPropertiesGet(String requestId, String serviceId);


}
