package com.huaweicloud.sdk.iot.device.gateway;

import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;

public interface SubDevDownlinkListener {

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
     * @param requestId 请求id
     * @param propsSet  属性设置
     */
    public abstract void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * 子设备读属性通知
     *
     * @param requestId 请求id
     * @param propsGet  属性查询
     */
    public abstract void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     * 子设备消息下发
     *
     * @param message 设备消息
     */
    public abstract void onSubdevMessage(DeviceMessage message);
}
