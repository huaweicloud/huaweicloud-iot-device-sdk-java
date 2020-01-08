package com.huaweicloud.sdk.iot.device;


import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.DeviceClientInner;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;

import java.util.List;


/**
 * 设备客户端类，提供设备到平台的通讯能力：
 * 消息：包括设备向平台上报消息，平台向设备下发消息，消息不需要建模，不需要响应
 * 属性：包括设备向平台上报属性，平台向设备设置、查询属性，属性需要建模，需要响应
 * 命令：平台向设备下发命令，命令需要建模，需要响应
 */
public class DeviceClient extends DeviceClientInner {

    /**
     * 构造函数
     *
     * @param clientConf 客户端配置参数
     */
    public DeviceClient(ClientConf clientConf, IoTDevice device) {
        super(clientConf, device);
    }


    /**
     * 和平台建立连接，此接口为阻塞调用，超时时长20s
     *
     * @return 0表示连接成功，其他表示连接失败
     */
    public int connect() {

        return super.connect();
    }


    /**
     * 上报设备消息
     *
     * @param deviceMessage 设备消息，必选
     * @param listener      监听器，可选
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener) {
        super.reportDeviceMessage(deviceMessage, listener);
    }


    /**
     * 发布原始消息，原始消息和设备消息区别是：原始消息用户可以自定义topic和消息格式
     *
     * @param rawMessage 原始消息
     * @param listener   监听器
     */
    public void publishRawMessage(RawMessage rawMessage, ActionListener listener) {
        super.publishRawMessage(rawMessage, listener);
    }


    /**
     * 向平台上报设备属性
     *
     * @param properties 设备属性列表
     * @param listener   发布监听器
     */
    public void reportProperties(List<ServiceProperty> properties, ActionListener listener) {

        super.reportProperties(properties, listener);

    }


    /**
     * 设置连接监听器，用于接收连接建立和断开通知
     *
     * @param connectListener 连接监听器
     */
    public void setConnectListener(ConnectListener connectListener) {
        super.setConnectListener(connectListener);
    }


    /**
     * 关闭到平台的连接
     */
    public void close() {
        super.close();
    }


    /**
     * 发送命令响应，用于在处理完平台下发的命令后向平台上报响应消息。所有命令都应该上报响应，否则平台
     * 会认为命令超时
     *
     * @param requestId  命令请求的标识，用来标识一个命令的请求和响应
     * @param commandRsp 命令的响应
     */
    public void respondCommand(String requestId, CommandRsp commandRsp) {

        super.respondCommand(requestId, commandRsp);
    }


    /**
     * 响应读属性
     *
     * @param requestId 请求id
     * @param services  服务属性列表
     */
    public void respondPropsGet(String requestId, List<ServiceProperty> services) {

        super.respondPropsGet(requestId, services);
    }

    /**
     * 上报属性设置的响应
     *
     * @param requestId 请求标识
     * @param iotResult 处理结果
     */
    public void respondPropsSet(String requestId, IotResult iotResult) {

        super.respondPropsSet(requestId, iotResult);
    }

    /**
     * 订阅自定义topic。注意平台定义的topic由SDK自动订阅。
     *
     * @param topic           自定义topic
     * @param actionListener  监听订阅结果
     * @param messageListener 原始消息监听器
     */
    public void subscribeTopic(String topic, ActionListener actionListener, RawMessageListener messageListener) {

        super.subscribeTopic(topic, actionListener, messageListener);
    }

    /**
     * 设置属性监听器，用于接收属性读写通知
     *
     * @param propertyListener 属性监听器
     */
    public void setPropertyListener(PropertyListener propertyListener) {

        super.setPropertyListener(propertyListener);
    }

    /**
     * 设置命令监听器，用于接收命令
     *
     * @param commandListener 命令监听器
     */
    public void setCommandListener(CommandListener commandListener) {
        super.setCommandListener(commandListener);
    }

    /**
     * 设置设备消息监听器，用于接收平台到设备的消息。注意用户自定义topic的消息不在这里接收
     *
     * @param deviceMessageListener 设备消息监听器
     */
    public void setDeviceMessageListener(DeviceMessageListener deviceMessageListener) {
        super.setDeviceMessageListener(deviceMessageListener);
    }

}
