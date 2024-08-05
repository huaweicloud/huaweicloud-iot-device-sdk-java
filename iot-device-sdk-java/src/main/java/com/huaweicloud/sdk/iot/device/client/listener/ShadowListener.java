package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.client.requests.ShadowData;

import java.util.List;

/**
 * 设备影子监听器，用于设备向平台获取设备影子数据
 */
public interface ShadowListener {
    /**
     *
     * @param requestId       请求id
     * @param shadowDataList  服务影子数据
     */
    void onShadow(String requestId, List<ShadowData> shadowDataList);
}
