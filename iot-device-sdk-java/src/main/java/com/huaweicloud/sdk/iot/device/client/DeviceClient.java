package com.huaweicloud.sdk.iot.device.client;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceProperties;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
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
public class DeviceClient implements RawMessageListener {

    private PropertyListener propertyListener;
    private CommandListener commandListener;
    private DeviceMessageListener deviceMessageListener;

    private ClientConf clientConf;
    private Connection connection;
    private RequestManager requestManager;
    private String deviceId;
    private Map<String, RawMessageListener> rawMessageListenerMap;
    private AbstractDevice device;

    private Logger log = Logger.getLogger(DeviceClient.class);

    public DeviceClient(ClientConf clientConf, AbstractDevice device) {

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
    public int connect() {
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

    public void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener) {
        String topic = "$oc/devices/" + deviceId + "/sys/messages/up";
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage)), listener);
    }

    public void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener, int qos) {
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
    public void publishRawMessage(RawMessage rawMessage, ActionListener listener) {
        connection.publishMessage(rawMessage, listener);
    }


    /**
     * 向平台上报设备属性
     *
     * @param properties 设备属性列表
     * @param listener   发布监听器
     */
    public void reportProperties(List<ServiceProperty> properties, ActionListener listener) {

        String deviceId = clientConf.getDeviceId();
        String topic = "$oc/devices/" + deviceId + "/sys/properties/report";
        ObjectNode jsonObject = JsonUtil.createObjectNode();
        jsonObject.putPOJO("services", properties);

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(jsonObject));
        connection.publishMessage(rawMessage, listener);

    }

    /**
     * 向平台上报设备属性
     *
     * @param deviceId   设备id
     * @param properties 设备属性列表
     * @param listener   发布监听器
     */
    public void reportProperties(String deviceId, List<ServiceProperty> properties, ActionListener listener) {

        String topic = "$oc/devices/" + deviceId + "/sys/properties/report";
        ObjectNode jsonObject = JsonUtil.createObjectNode();
        jsonObject.putPOJO("services", properties);

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(jsonObject));
        connection.publishMessage(rawMessage, listener);

    }

    private void OnPropertiesSet(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsSet propsSet = JsonUtil.convertJsonStringToObject(message.toString(), PropsSet.class);
        if (propsSet == null) {
            return;
        }

        if (propertyListener != null && (propsSet.getDeviceId() == null || propsSet.getDeviceId().equals(getDeviceId()))) {

            propertyListener.onPropertiesSet(requestId, propsSet.getServices());
            return;

        }

        device.onPropertiesSet(requestId, propsSet);
    }

    private void OnPropertiesGet(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsGet propsGet = JsonUtil.convertJsonStringToObject(message.toString(), PropsGet.class);
        if (propsGet == null) {
            return;
        }

        if (propertyListener != null && (propsGet.getDeviceId() == null || propsGet.getDeviceId().equals(getDeviceId()))) {
            propertyListener.onPropertiesGet(requestId, propsGet.getServiceId());
            return;
        }

        device.onPropertiesGet(requestId, propsGet);

    }


    private void onCommand(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        Command command = JsonUtil.convertJsonStringToObject(message.toString(), Command.class);
        if (command == null) {
            log.error("invalid command");
            return;
        }

        if (commandListener != null && (command.getDeviceId() == null || command.getDeviceId().equals(getDeviceId()))) {
            commandListener.onCommand(requestId, command.getServiceId(),
                    command.getCommandName(), command.getParas());
            return;
        }

        device.onCommand(requestId, command);

    }


    private void onDeviceMessage(RawMessage message) {
        DeviceMessage deviceMessage = JsonUtil.convertJsonStringToObject(message.toString(),
                DeviceMessage.class);
        if (deviceMessage == null) {
            log.error("invalid deviceMessage: " + message.toString());
            return;
        }

        if (deviceMessageListener != null && (deviceMessage.getDeviceId() == null || deviceMessage.getDeviceId().equals(getDeviceId()))) {
            deviceMessageListener.onDeviceMessage(deviceMessage);
            return;
        }
        device.onDeviceMessage(deviceMessage);
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


    public void close() {
        connection.close();
    }


    public void respondCommand(String requestId, CommandRsp commandRsp) {

        String topic = "$oc/devices/" + deviceId + "/sys/commands/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRsp));
        connection.publishMessage(rawMessage, null);
    }

    public void respondPropsGet(String requestId, List<ServiceProperty> services) {

        DeviceProperties deviceProperties = new DeviceProperties();
        deviceProperties.setServices(services);

        String topic = "$oc/devices/" + deviceId + "/sys/properties/get/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceProperties));
        connection.publishMessage(rawMessage, null);
    }

    public void respondPropsSet(String requestId, IotResult iotResult) {

        String topic = "$oc/devices/" + deviceId + "/sys/properties/set/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(iotResult));
        connection.publishMessage(rawMessage, null);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setConnectListener(ConnectListener connectListener) {
        connection.setConnectListener(connectListener);
    }

    public void subscribeTopic(String topic, ActionListener actionListener, RawMessageListener rawMessageListener) {
        connection.subscribeTopic(topic, actionListener);
        rawMessageListenerMap.put(topic, rawMessageListener);
    }

    public void setPropertyListener(PropertyListener propertyListener) {
        this.propertyListener = propertyListener;
    }

    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    public void setDeviceMessageListener(DeviceMessageListener deviceMessageListener) {
        this.deviceMessageListener = deviceMessageListener;
    }

    public void setDevice(AbstractDevice device) {
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
