package com.huaweicloud.sdk.iot.device.client;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.listener.CommandV3Listener;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRspV3;
import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceProperties;
import com.huaweicloud.sdk.iot.device.client.requests.DevicePropertiesV3;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceProperty;
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
import java.util.concurrent.*;


/**
 * 设备客户端，提供和平台的通讯能力，包括：
 * 消息：双向，异步，不需要定义模型
 * 属性：双向，设备可以上报属性，平台可以向设备读写属性，属性需要在模型定义
 * 命令：单向，同步，平台向设备调用设备的命令
 * 事件：双向、异步，需要在模型定义
 * 用户不能直接创建DeviceClient实例，只能先创建IoTDevice实例，然后通过IoTDevice的getClient接口获取DeviceClient实例
 */
public class DeviceClient implements RawMessageListener {
    private static final Logger log = Logger.getLogger(DeviceClient.class);

    private PropertyListener propertyListener;
    private CommandListener commandListener;
    private CommandV3Listener commandV3Listener;
    private DeviceMessageListener deviceMessageListener;

    private ClientConf clientConf;
    private Connection connection;
    private RequestManager requestManager;
    private String deviceId;
    private Map<String, RawMessageListener> rawMessageListenerMap;
    private AbstractDevice device;

    private ScheduledExecutorService executorService;
    private int ClientThreadCount = 1;
    public static int connectFailedTime = 0;

    public DeviceClient(ClientConf clientConf, AbstractDevice device) {
        checkClientConf(clientConf);
        this.clientConf = clientConf;
        this.deviceId = clientConf.getDeviceId();
        this.requestManager = new RequestManager(this);
        this.connection = new MqttConnection(clientConf, this);
        this.device = device;
        this.rawMessageListenerMap = new ConcurrentHashMap<>();

    }

    public ClientConf getClientConf() {
        return clientConf;
    }

    private void checkClientConf(ClientConf clientConf) throws IllegalArgumentException {
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
     * 和平台建立连接，此接口为阻塞调用，超时时长60s。连接成功时，SDK会自动向平台订阅系统定义的topic。
     *
     * @return 0表示连接成功，其他表示连接失败
     */
    public int connect() {

        synchronized (this) {
            if (executorService == null) {
                executorService = Executors.newScheduledThreadPool(ClientThreadCount);
            }
        }

        int ret = connection.connect();
        //退避机制重连
        while (ret != 0) {
            connectFailedTime++;
            try {
                if (connectFailedTime < 10) {
                    Thread.sleep(500);
                } else if (connectFailedTime < 50) {
                    Thread.sleep(5000);
                } else {
                    Thread.sleep(10000);
                }
                ret = connection.connect();
            } catch (InterruptedException e) {
                log.debug("connect failed" + connectFailedTime + "times");
            }
        }

        connectFailedTime = 0;

        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/messages/down", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/commands/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/set/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/get/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/shadow/get/response/#", null);
        connection.subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/events/down", null);
        connection.subscribeTopic("/huawei/v1/devices/" + clientConf.getDeviceId() + "/command/json", null);
        connection.subscribeTopic("/huawei/v1/devices/" + clientConf.getDeviceId() + "/command/binary", null);

        return ret;
    }

    /**
     * 上报设备消息
     * 如果需要上报子设备消息，需要调用DeviceMessage的setDeviceId接口设置为子设备的设备id
     *
     * @param deviceMessage 设备消息
     * @param listener      监听器，用于接收上报结果
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage)), listener);
    }

    /**
     * 上报设备消息，支持指定qos
     *
     * @param deviceMessage 设备消息
     * @param listener      监听器，用于接收上报结果
     * @param qos           消息qos，0或1
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener, int qos) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        if (qos != 0) {
            qos = 1;
        }
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage), qos), listener);
    }


    /**
     * 发布原始消息，原始消息和设备消息（DeviceMessage）的区别是：
     * 1、可以自定义topic，该topic需要在平台侧配置
     * 2、不限制payload的格式
     *
     * @param rawMessage 原始消息
     * @param listener   监听器
     */
    public void publishRawMessage(RawMessage rawMessage, ActionListener listener) {
        connection.publishMessage(rawMessage, listener);
    }


    /**
     * 上报设备属性
     *
     * @param properties 设备属性列表
     * @param listener   发布监听器
     */
    public void reportProperties(List<ServiceProperty> properties, ActionListener listener) {

        String topic = "$oc/devices/" + this.deviceId + "/sys/properties/report";
        ObjectNode jsonObject = JsonUtil.createObjectNode();
        jsonObject.putPOJO("services", properties);

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(jsonObject));
        connection.publishMessage(rawMessage, listener);

    }

    /**
     * 向平台上报设备属性（V3接口）
     *
     * @param devicePropertiesV3 设备上报的属性
     * @param listener           发布监听器
     */
    public void reportPropertiesV3(DevicePropertiesV3 devicePropertiesV3, ActionListener listener) {
        String topic = "/huawei/v1/devices/" + this.deviceId + "/data/json";

        RawMessage rawMessage = new RawMessage(topic, devicePropertiesV3.toString());
        connection.publishMessage(rawMessage, listener);
    }

    /**
     * 向平台上报设备属性（V3接口）
     *
     * @param bytes    设备上报的码流
     * @param listener 发布监听器
     */
    public void reportBinaryV3(Byte[] bytes, ActionListener listener) {

        String deviceId = clientConf.getDeviceId();
        String topic = "/huawei/v1/devices/" + deviceId + "/data/binary";

        RawMessage rawMessage = new RawMessage(topic, Arrays.toString(bytes));
        connection.publishMessage(rawMessage, listener);
    }

    /**
     * 向平台上报V3命令响应
     *
     * @param commandRspV3 命令响应结果
     * @param listener     发布监听器
     */
    public void responseCommandV3(CommandRspV3 commandRspV3, ActionListener listener) {

        String topic = "/huawei/v1/devices/" + deviceId + "/data/json";
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRspV3));
        connection.publishMessage(rawMessage, listener);
    }

    /**
     * 向平台上报V3命令响应（码流）
     *
     * @param bytes    响应码流
     * @param listener 发布监听器
     */
    public void responseCommandBinaryV3(Byte[] bytes, ActionListener listener) {

        String topic = "/huawei/v1/devices/" + deviceId + "/data/binary";
        RawMessage rawMessage = new RawMessage(topic, Arrays.toString(bytes));
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
            return;
        }

        //只处理直连设备的，子设备的由AbstractGateway处理
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

    private void onCommandV3(RawMessage message) {
        CommandV3 commandV3 = JsonUtil.convertJsonStringToObject(message.toString(), CommandV3.class);
        if (commandV3 == null) {
            log.error("invalid commandV3");
            return;
        }

        if (commandV3Listener != null) {
            commandV3Listener.onCommandV3(commandV3);
        }
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

        if (executorService == null) {
            log.error("executionService is null");
            return;
        }

        executorService.schedule(new Runnable() {
            @Override
            public void run() {
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
                    } else if (topic.contains("/huawei/v1/devices") && topic.contains("/command/")) {
                        onCommandV3(message);
                    } else {
                        log.error("unknown topic: " + topic);
                    }

                } catch (Exception e) {
                    log.error(ExceptionUtil.getBriefStackTrace(e));
                }
            }
        }, 0, TimeUnit.MILLISECONDS);

    }


    public void close() {
        connection.close();
    }


    /**
     * 上报命令响应
     *
     * @param requestId  请求id，响应的请求id必须和请求的一致
     * @param commandRsp 命令响应
     */
    public void respondCommand(String requestId, CommandRsp commandRsp) {

        String topic = "$oc/devices/" + deviceId + "/sys/commands/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRsp));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 上报读属性响应
     *
     * @param requestId 请求id，响应的请求id必须和请求的一致
     * @param services  服务属性
     */
    public void respondPropsGet(String requestId, List<ServiceProperty> services) {

        DeviceProperties deviceProperties = new DeviceProperties();
        deviceProperties.setServices(services);

        String topic = "$oc/devices/" + deviceId + "/sys/properties/get/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceProperties));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 上报写属性响应
     *
     * @param requestId 请求id，响应的请求id必须和请求的一致
     * @param iotResult 写属性结果
     */
    public void respondPropsSet(String requestId, IotResult iotResult) {

        String topic = "$oc/devices/" + deviceId + "/sys/properties/set/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(iotResult));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 获取设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置连接监听器，用户接收连接建立和断开事件
     *
     * @param connectListener
     */
    public void setConnectListener(ConnectListener connectListener) {
        connection.setConnectListener(connectListener);
    }

    /**
     * 订阅自定义topic。系统topic由SDK自动订阅，此接口只能用于订阅自定义topic
     *
     * @param topic              自定义topic
     * @param actionListener     订阅结果监听器
     * @param rawMessageListener 接收自定义消息的监听器
     */
    public void subscribeTopic(String topic, ActionListener actionListener, RawMessageListener rawMessageListener) {
        connection.subscribeTopic(topic, actionListener);
        rawMessageListenerMap.put(topic, rawMessageListener);
    }

    /**
     * 设置属性监听器，用于接收平台下发的属性读写。
     * 此监听器只能接收平台到直连设备的请求，子设备的请求由AbstractGateway处理
     *
     * @param propertyListener 属性监听器
     */
    public void setPropertyListener(PropertyListener propertyListener) {
        this.propertyListener = propertyListener;
    }

    /**
     * 设置命令监听器，用于接收平台下发的命令。
     * 此监听器只能接收平台到直连设备的请求，子设备的请求由AbstractGateway处理
     *
     * @param commandListener 命令监听器
     */
    public void setCommandListener(CommandListener commandListener) {
        this.commandListener = commandListener;
    }

    /**
     * 设置消息监听器，用于接收平台下发的消息
     * 此监听器只能接收平台到直连设备的请求，子设备的请求由AbstractGateway处理
     *
     * @param deviceMessageListener 消息监听器
     */
    public void setDeviceMessageListener(DeviceMessageListener deviceMessageListener) {
        this.deviceMessageListener = deviceMessageListener;
    }

    /**
     * 设置命令监听器，用于接收V3命令
     *
     * @param commandV3Listener 命令监听器
     */
    public void setCommandV3Listener(CommandV3Listener commandV3Listener) {
        this.commandV3Listener = commandV3Listener;
    }


    public void setDevice(AbstractDevice device) {
        this.device = device;
    }

    /**
     * 事件上报
     *
     * @param event    事件
     * @param listener 监听器
     */
    public void reportEvent(DeviceEvent event, ActionListener listener) {

        DeviceEvents events = new DeviceEvents();
        events.setDeviceId(getDeviceId());
        events.setServices(Arrays.asList(event));
        String deviceId = clientConf.getDeviceId();
        String topic = "$oc/devices/" + deviceId + "/sys/events/up";

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(events));
        connection.publishMessage(rawMessage, listener);

    }

    public Future<?> scheduleTask(Runnable runnable) {
        return executorService.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    public Future<?> scheduleTask(Runnable runnable, long delay) {
        return executorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public Future<?> scheduleRoutineTask(Runnable runnable, long period) {
        return executorService.scheduleAtFixedRate(runnable, period, period, TimeUnit.MILLISECONDS);
    }

    public int getClientThreadCount() {
        return ClientThreadCount;
    }

    public void setClientThreadCount(int clientThreadCount) {
        ClientThreadCount = clientThreadCount;
    }
}
