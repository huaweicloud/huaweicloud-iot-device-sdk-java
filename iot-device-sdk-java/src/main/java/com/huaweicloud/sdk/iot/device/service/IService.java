package com.huaweicloud.sdk.iot.device.service;

import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;

import java.util.Map;

/**
 * 服务接口类
 */
public interface IService {
    /**
     * 读属性回调
     *
     * @param fields 指定读取的字段名，不指定则读取全部可读字段
     * @return 属性值，json格式
     */
    Map<String, Object> onRead(String... fields);

    /**
     * 写属性回调
     *
     * @param properties 属性期望值
     * @return 操作结果jsonObject
     */
    IotResult onWrite(Map<String, Object> properties);

    /**
     * 命令回调
     *
     * @param command 命令
     * @return 执行结果
     */
    CommandRsp onCommand(Command command);

    /**
     * 事件回调
     *
     * @param deviceEvent 事件
     */
    void onEvent(DeviceEvent deviceEvent);
}
