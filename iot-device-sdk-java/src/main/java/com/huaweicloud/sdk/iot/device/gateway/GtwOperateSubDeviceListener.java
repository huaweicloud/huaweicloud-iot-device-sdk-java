package com.huaweicloud.sdk.iot.device.gateway;

import com.huaweicloud.sdk.iot.device.gateway.requests.GtwAddSubDeviceRsp;
import com.huaweicloud.sdk.iot.device.gateway.requests.GtwDelSubDeviceRsp;

public interface GtwOperateSubDeviceListener {

    /**
     * 处理网关增加子设备返回结果
     *
     * @param gtwAddSubDeviceRsp    网关增加子设备响应
     * @param eventId               事件Id
     */
    void onAddSubDeviceRsp(GtwAddSubDeviceRsp gtwAddSubDeviceRsp, String eventId);

    /**
     * 处理网关删除子设备返回结果
     *
     * @param gtwDelSubDeviceRsp    网关删除子设备响应
     * @param eventId               事件Id
     */
    void onDelSubDeviceRsp(GtwDelSubDeviceRsp gtwDelSubDeviceRsp, String eventId);
}
