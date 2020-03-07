package com.huaweicloud.sdk.iot.device.client;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.listener.SubDeviceDownlinkListener;
import com.huaweicloud.sdk.iot.device.client.requests.*;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceProperty;
import com.huaweicloud.sdk.iot.device.transport.*;
import com.huaweicloud.sdk.iot.device.transport.mqtt.MqttConnection;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * DeviceClient的内部实现
 */
public class DeviceClientInner implements RawMessageListener {

    private PropertyListener propertyListener;
    private CommandListener commandListener;
    private DeviceMessageListener deviceMessageListener;
    private SubDeviceDownlinkListener subDeviceDownlinkListener;

    private ClientConf clientConf;
    private Connection connection;
    private RequestManager requestManager;
    private String deviceId;
    private Map<String, RawMessageListener> rawMessageListenerMap;
    private IoTDevice device;

    private Logger log = Logger.getLogger(DeviceClientInner.class);

    public DeviceClientInner(ClientConf clientConf, IoTDevice device) {

        this.clientConf = clientConf;
        checkClientConf();
        this.deviceId = clientConf.getDeviceId();
        this.requestManager = new RequestManager(this);
        this.connection = new MqttConnection(clientConf, this);
        this.device = device;
        this.rawMessageListenerMap = new ConcurrentHashMap<>();

    }

    public ClientConf getClientConf() {
        return clientConf;
    }

    private void checkClientConf() throws IllegalArgumentException {
        if (clientConf == null) {
            throw new IllegalArgumentException("clientConf is null");
        }
        if (clientConf.getDeviceId() == null) {
            throw new IllegalArgumentException("clientConf.getDeviceId() is null");
        }
        if (clientConf.getSecret() == null && clientConf.getKeyStore() == null) {
            throw new IllegalArgumentException("secret and keystore is null");
        }
        if (clientConf.getServerUri() == null) {
            throw new IllegalArgumentException("clientConf.getSecret() is null");
        }
        if (!clientConf.getServerUri().startsWith("tcp://") && (!clientConf.getServerUri().startsWith("ssl://"))) {
            throw new IllegalArgumentException("invalid serverUri");
        }
    }

    /**
     * 和平台建立连接，此接口为阻塞调用，超时时长20s
     *
     * @return 0表示连接成功，其他表示连接失败
     */
    protected int connect() {
        int ret = connection.connect();
        if (ret != 0) {
            return ret;
        }

        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/messages/down", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/commands/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/set/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/get/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/shadow/get/response/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/events/down", null);

        return ret;
    }


    protected void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener, int qos) {
        String topic = "$oc/devices/" + deviceId + "/sys/messages/up";
        if (qos != 0) {
            qos = 1;
        }
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage), qos), listener);
    }


    /**
     * 发布原始消息，原始消息用户可以指定topic
     *
     * @param rawMessage 原始消息
     * @param listener   监听器
     */
    protected void publishRawMessage(RawMessage rawMessage, ActionListener listener) {
        connection.publishMessage(rawMessage, listener);
    }


    /**
     * 向平台上报设备属性
     *
     * @param properties 设备属性列表
     * @param listener   发布监听器
     */
    protected void reportProperties(List<ServiceProperty> properties, ActionListener listener) {

        String deviceId = clientConf.getDeviceId();
        String topic = "$oc/devices/" + deviceId + "/sys/properties/report";
        ObjectNode jsonObject = JsonUtil.createObjectNode();
        jsonObject.putPOJO("services", properties);

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(jsonObject));
        connection.publishMessage(rawMessage, listener);

    }


    /**
     * 批量上报子设备属性
     *
     * @param deviceProperties 子设备属性列表
     * @param listener         发布监听器
     */
    public void reportSubDeviceProperties(List<DeviceProperty> deviceProperties,
                                          ActionListener listener) {


        ObjectNode node = JsonUtil.createObjectNode();
        node.putPOJO("devices", deviceProperties);

        String topic = "$oc/devices/" + getDeviceId() + "/sys/gateway/sub_devices/properties/report";

        RawMessage rawMessage = new RawMessage(topic, node.toString());

        publishRawMessage(rawMessage, listener);

    }

    private void OnPropertiesSet(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsSet propsSet = JsonUtil.convertJsonStringToObject(message.toString(), PropsSet.class);
        if (propsSet == null) {
            log.error("propsSet is null");
            return;
        }

        //子设备的
        if (propsSet.getDeviceId() != null && !propsSet.getDeviceId().equals(this.deviceId)) {
            if (subDeviceDownlinkListener != null) {
                subDeviceDownlinkListener.onPropertiesSet(requestId, propsSet.getServices(), propsSet.getDeviceId());
                return;
            }
        }

        device.onPropertiesSet(requestId, propsSet);

        if (propertyListener != null) {

            propertyListener.onPropertiesSet(requestId, propsSet.getServices());
        }

    }

    private void OnPropertiesGet(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsGet propsGet = JsonUtil.convertJsonStringToObject(message.toString(), PropsGet.class);
        if (propsGet == null) {
            log.error("propsGet is null");
            return;
        }

        //子设备的
        if (propsGet.getDeviceId() != null && !propsGet.getDeviceId().equals(this.deviceId)) {
            if (subDeviceDownlinkListener != null) {
                subDeviceDownlinkListener.onPropertiesGet(requestId, propsGet.getServiceId(), propsGet.getDeviceId());
                return;
            }
        }

        device.onPropertiesGet(requestId, propsGet);

        if (propertyListener != null) {
            propertyListener.onPropertiesGet(requestId, propsGet.getServiceId());
        }

    }


    private void onCommand(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        DeviceCommand command = JsonUtil.convertJsonStringToObject(message.toString(), DeviceCommand.class);
        if (command == null) {
            log.error("invalid command");
            return;
        }

        //子设备的
        if (command.getDeviceId() != null && !command.getDeviceId().equals(this.deviceId)) {
            if (subDeviceDownlinkListener != null) {
                subDeviceDownlinkListener.onCommand(requestId, command);
                return;
            }
        }

        device.onCommand(requestId, command);

        if (commandListener != null) {
            commandListener.onCommand(requestId, command.getServiceId(), command.getCommandName(), command.getParas());

        }

    }


    private void onDeviceMessage(RawMessage message) {
        DeviceMessage deviceMessage = JsonUtil.convertJsonStringToObject(message.toString(),
                DeviceMessage.class);
        if (deviceMessage == null) {
            log.error("invalid deviceMessage: " + message.toString());
            return;
        }

        //子设备的
        if (deviceMessage.getDeviceId() != null && !deviceMessage.getDeviceId().equals(this.deviceId)) {
            if (subDeviceDownlinkListener != null) {
                subDeviceDownlinkListener.onDeviceMessage(deviceMessage);
                return;
            }
        }

        if (deviceMessageListener != null) {
            deviceMessageListener.onDeviceMessage(deviceMessage);
        }
    }

    private void onEvent(RawMessage message) {

        DeviceEvents deviceEvents = JsonUtil.convertJsonStringToObject(message.toString(), DeviceEvents.class);
        if (deviceEvents == null) {
            log.error("invalid events");
            return;
        }
        device.onEvent(deviceEvents);
    }

    private void onResponse(RawMessage message) {
        requestManager.onRequestResponse(message);
    }


    @Override
    public void onMessageReceived(RawMessage message) {

        try {
            String topic = message.getTopic();

            RawMessageListener listener = rawMessageListenerMap.get(topic);
            if (listener != null) {
                listener.onMessageReceived(message);
                return;
            }

            if (topic.contains("/messages/down")) {
                onDeviceMessage(message);
            } else if (topic.contains("sys/commands/request_id")) {
                onCommand(message);

            } else if (topic.contains("/sys/properties/set/request_id")) {
                OnPropertiesSet(message);

            } else if (topic.contains("/sys/properties/get/request_id")) {
                OnPropertiesGet(message);

            } else if (topic.contains("/desired/properties/get/response")) {
                onResponse(message);
            } else if (topic.contains("/sys/events/down")) {
                onEvent(message);
            }

        } catch (Exception e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }

    }


    protected void close() {
        connection.close();
    }


    protected void respondCommand(String requestId, CommandRsp commandRsp) {

        String topic = "$oc/devices/" + deviceId + "/sys/commands/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRsp));
        connection.publishMessage(rawMessage, null);
    }

    protected void respondPropsGet(String requestId, List<ServiceProperty> services) {

        DeviceProperties deviceProperties = new DeviceProperties();
        deviceProperties.setServices(services);

        String topic = "$oc/devices/" + deviceId + "/sys/properties/get/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceProperties));
        connection.publishMessage(rawMessage, null);
    }

    protected void respondPropsSet(String requestId, IotResult iotResult) {

        String topic = "$oc/devices/" + deviceId + "/sys/properties/set/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(iotResult));
        connection.publishMessage(rawMessage, null);
    }

    public String getDeviceId() {
        return deviceId;
    }

    protected void setConnectListener(ConnectListener connectListener) {
        connection.setConnectListener(connectListener);
    }

    protected void subscribeTopic(String topic, ActionListener actionListener, RawMessageListener rawMessageListener) {
        connection.subscribeTopic(topic, actionListener);
        rawMessageListenerMap.put(topic, rawMessageListener);
    }

    protected void setPropertyListener(PropertyListener propertyListener) {
        this.propertyListener = propertyListener;
    }

    protected void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    protected void setDeviceMessageListener(DeviceMessageListener deviceMessageListener) {
        this.deviceMessageListener = deviceMessageListener;
    }

    public void setSubDeviceDownlinkListener(SubDeviceDownlinkListener subDeviceDownlinkListener) {
        this.subDeviceDownlinkListener = subDeviceDownlinkListener;
    }

    public void setDevice(IoTDevice device) {
        this.device = device;
    }

    public void reportEvent(DeviceEvent event, ActionListener listener) {

        DeviceEvents events = new DeviceEvents();
        events.setDeviceId(getDeviceId());
        events.setServices(Arrays.asList(event));
        String deviceId = clientConf.getDeviceId();
        String topic = "$oc/devices/" + deviceId + "/sys/events/up";

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(events));
        connection.publishMessage(rawMessage, listener);

    }
}
