package com.huaweicloud.sdk.iot.device.client.handler;

import com.huaweicloud.sdk.iot.device.transport.Connection;

public interface CustomBackoffHandler {
    /**
     * 自定义断线重连
     * @param connection     IOT连接，代表设备和平台之间的一个连接
     * @return 0 代表重连成功， 其他代表重连失败
     */
    int backoffHandler(Connection connection);
}
