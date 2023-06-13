package com.huaweicloud.bridge.sdk;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.bridge.sdk.bootstrap.BridgeBootstrap;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.bridge.sdk.handler.BridgeCommandHandler;
import com.huaweicloud.bridge.sdk.handler.BridgeMessageHandler;
import com.huaweicloud.bridge.sdk.handler.BridgePropertyGetHandler;
import com.huaweicloud.bridge.sdk.handler.BridgePropertySetHandler;
import com.huaweicloud.bridge.sdk.handler.DeviceDisConnHandler;
import com.huaweicloud.bridge.sdk.handler.DeviceLoginHandler;
import com.huaweicloud.bridge.sdk.handler.DeviceLogoutHandler;
import com.huaweicloud.bridge.sdk.handler.SecretResetHandler;
import com.huaweicloud.bridge.sdk.listener.BridgeCommandListener;
import com.huaweicloud.bridge.sdk.listener.BridgeDeviceDisConnListener;
import com.huaweicloud.bridge.sdk.listener.BridgeDeviceMessageListener;
import com.huaweicloud.bridge.sdk.listener.BridgePropertyListener;
import com.huaweicloud.bridge.sdk.listener.ResetDeviceSecretListener;
import com.huaweicloud.bridge.sdk.request.RequestIdCache;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.bridge.sdk.listener.LoginListener;
import com.huaweicloud.bridge.sdk.listener.LogoutListener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.bridge.sdk.request.DeviceSecret;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceProperties;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BridgeClient extends DeviceClient {

    private static final Logger log = LogManager.getLogger(BridgeBootstrap.class);

    private static final String SIGN_TYPE = "sign_type";

    private static final String TIMESTAMP = "timestamp";

    private static final String PASSWORD = "password";

    /**
     * bridgeClient相关请求topic
     */

    private static final String BRIDGE_LOGIN = "$oc/bridges/%s/devices/%s/sys/login/request_id=%s";

    private static final String BRIDGE_LOGOUT = "$oc/bridges/%s/devices/%s/sys/logout/request_id=%s";

    private static final String BRIDGE_REPORT_PROPERTY = "$oc/bridges/%s/devices/%s/sys/properties/report";

    private static final String BRIDGE_RESET_DEVICE_SECRET = "$oc/bridges/%s/devices/%s/sys/reset_secret/request_id=%s";

    private static final String BRIDGE_REPORT_MESSAGE = "$oc/bridges/%s/devices/%s/sys/messages/up";

    private static final String BRIDEGE_EVENT = "$oc/bridges/%s/devices/%s/sys/events/up";

    private static final String BRIDGE_COMMAND_RESPONSE
        = "$oc/bridges/%s/devices/%s/sys/commands/response/request_id=%s";

    private static final String BRIDGE_PROP_SET_RESPONSE
        = "$oc/bridges/%s/devices/%s/sys/properties/set/response/request_id=%s";

    private static final String BRIDGE_PROP_GET_RESPONSE
        = "$oc/bridges/%s/devices/%s/sys/properties/get/response/request_id=%s";

    /**
     * bridgeClient相关的响应topic
     */

    private static final String MESSAGE_DOWN_TOPIC = "/messages/down";

    private static final String COMMAND_DOWN_TOPIC = "/sys/commands/request_id";

    private static final String LOGIN_RESP_TOPIC = "/sys/login/response/request_id";

    private static final String LOGOUT_RESP_TOPIC = "/sys/logout/response/request_id";

    private static final String BRIDGE_RESET_DEVICE_SECRET_RESP = "/sys/reset_secret/response/request_id";

    private static final String BRIDGE_DEVICE_DISCONNECT = "/sys/disconnect";

    private static final String PROPERTY_SET_TOPIC = "/sys/properties/set/request_id";

    private static final String PROPERTY_GET_TOPIC = "/sys/properties/get/request_id";

    private String bridgeId;

    /**
     * bridge相关listener
     */
    private BridgeCommandListener bridgeCommandListener;

    private BridgeDeviceMessageListener bridgeDeviceMessageListener;

    private LoginListener loginListener;

    private LogoutListener logoutListener;

    private ResetDeviceSecretListener resetDeviceSecretListener;

    private BridgeDeviceDisConnListener bridgeDeviceDisConnListener;

    private BridgePropertyListener bridgePropertyListener;

    /**
     * 网关设备登录requestId对应缓存
     */
    private RequestIdCache requestIdCache;

    public BridgeClient() {
        super();
    }

    BridgeClient(ClientConf clientConf,
        AbstractDevice device) {
        super(clientConf, device);
        this.bridgeId = clientConf.getDeviceId();
        Map<String, MessageReceivedHandler> functionMap = getFunctionMap();
        requestIdCache = new RequestIdCache();
        functionMap.put(MESSAGE_DOWN_TOPIC, new BridgeMessageHandler(this));
        functionMap.put(COMMAND_DOWN_TOPIC, new BridgeCommandHandler(this));
        functionMap.put(LOGIN_RESP_TOPIC, new DeviceLoginHandler(this));
        functionMap.put(LOGOUT_RESP_TOPIC, new DeviceLogoutHandler(this));
        functionMap.put(BRIDGE_RESET_DEVICE_SECRET_RESP, new SecretResetHandler(this));
        functionMap.put(BRIDGE_DEVICE_DISCONNECT, new DeviceDisConnHandler(this));
        functionMap.put(PROPERTY_SET_TOPIC, new BridgePropertySetHandler(this));
        functionMap.put(PROPERTY_GET_TOPIC, new BridgePropertyGetHandler(this));
    }

    /**
     * 获取网桥处理命令下发的监听器
     *
     * @return 监听器
     */
    public BridgeCommandListener getBridgeCommandListener() {
        return bridgeCommandListener;
    }

    /**
     * 设置网桥命令监听器，用于接收平台下发的命令
     *
     * @param bridgeCommandListener 网桥监听器，用户可以自定义
     * @return 网桥客户端
     */
    public BridgeClient setBridgeCommandListener(
        BridgeCommandListener bridgeCommandListener) {
        if (bridgeCommandListener == null) {
            log.warn("the bridgeCommandListener is null.");
            return this;
        }
        this.bridgeCommandListener = bridgeCommandListener;
        return this;
    }

    /**
     * 获取网桥处理消息下发的监听器
     *
     * @return 监听器
     */
    public BridgeDeviceMessageListener getBridgeDeviceMessageListener() {
        return bridgeDeviceMessageListener;
    }

    /**
     * 设置网桥处理消息下发的监听器
     *
     * @param bridgeDeviceMessageListener 监听器
     * @return 网桥客户端
     */
    public BridgeClient setBridgeDeviceMessageListener(
        BridgeDeviceMessageListener bridgeDeviceMessageListener) {
        if (bridgeDeviceMessageListener == null) {
            log.warn("the bridgeDeviceMessageListener is null.");
            return this;
        }
        this.bridgeDeviceMessageListener = bridgeDeviceMessageListener;
        return this;
    }

    /**
     * 获取网桥设备登录监听器
     *
     * @return 监听器
     */
    public LoginListener getLoginListener() {
        return loginListener;
    }

    /**
     * 设置网桥设备登录监听器
     *
     * @param loginListener 监听器
     * @return 网桥客户端
     */
    public BridgeClient setLoginListener(LoginListener loginListener) {
        if (loginListener == null) {
            log.warn("the loginListener is null.");
            return this;
        }
        this.loginListener = loginListener;
        return this;
    }

    /**
     * 获取网桥属性查询/设置监听器
     *
     * @return 监听器
     */
    public BridgePropertyListener getBridgePropertyListener() {
        return bridgePropertyListener;
    }

    /**
     * 设置网桥属性查询/设置监听器
     *
     * @param bridgePropertyListener
     * @return 网桥客户端
     */
    public BridgeClient setBridgePropertyListener(BridgePropertyListener bridgePropertyListener) {
        if (bridgePropertyListener == null) {
            log.warn("the bridgePropertyListener is null.");
            return this;
        }
        this.bridgePropertyListener = bridgePropertyListener;
        return this;
    }

    /**
     * 获取网桥设备登出监听器
     *
     * @return 监听器
     */
    public LogoutListener getLogoutListener() {
        return logoutListener;
    }

    /**
     * 设置网桥设备登出监听器
     *
     * @param logoutListener 监听器
     * @return 网桥客户端
     */
    public BridgeClient setLogoutListener(LogoutListener logoutListener) {
        if (logoutListener == null) {
            log.warn("the logoutListener is null.");
            return this;
        }
        this.logoutListener = logoutListener;
        return this;
    }

    /**
     * 获取网桥重置设备密钥监听器
     *
     * @return 监听器内容
     */
    public ResetDeviceSecretListener getResetDeviceSecretListener() {
        return resetDeviceSecretListener;
    }

    /**
     * 设置网桥重置设备密钥监听器，用于处理平台下发的结果
     *
     * @param resetDeviceSecretListener 监听器
     * @return 网桥客户端
     */
    public BridgeClient setResetDeviceSecretListener(
        ResetDeviceSecretListener resetDeviceSecretListener) {
        if (resetDeviceSecretListener == null) {
            log.warn("the resetDeviceSecretListener is null.");
            return this;
        }
        this.resetDeviceSecretListener = resetDeviceSecretListener;
        return this;
    }

    /**
     * 获取网桥处理设备断链的监听器
     *
     * @return 监听器
     */
    public BridgeDeviceDisConnListener getBridgeDeviceDisConnListener() {
        return bridgeDeviceDisConnListener;
    }

    /**
     * 设置网桥处理设备断链的监听器
     *
     * @param bridgeDeviceDisConnListener 监听器
     * @return 网桥客户端
     */
    public BridgeClient setBridgeDeviceDisConnListener(
        BridgeDeviceDisConnListener bridgeDeviceDisConnListener) {
        if (bridgeDeviceDisConnListener == null) {
            log.warn("the bridgeDeviceDisConnListener is null.");
            return this;
        }
        this.bridgeDeviceDisConnListener = bridgeDeviceDisConnListener;
        return this;
    }

    public RequestIdCache getRequestIdCache() {
        return requestIdCache;
    }

    public void loginAsync(String deviceId, String password, String requestId, ActionListener listener) {
        RawMessage rawMessage = generateLoginMsg(deviceId, password, requestId);
        connection.publishMessage(rawMessage, listener);
    }

    public int loginSync(String deviceId, String password, int millisecondTimeout) {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Integer> future = new CompletableFuture<>();
        RawMessage rawMessage = generateLoginMsg(deviceId, password, requestId);
        return getSyncResult(millisecondTimeout, requestId, future, rawMessage);
    }

    private int getSyncResult(int millisecondTimeout, String requestId, CompletableFuture<Integer> future,
        RawMessage rawMessage) {
        requestIdCache.setRequestId2Cache(requestId, future);
        connection.publishMessage(rawMessage, null);

        try {
            return future.get(millisecondTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }

        return -1;
    }

    private RawMessage generateLoginMsg(String deviceId, String password, String requestId) {
        String timeStamp = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
            .format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

        String secret = null;
        if (password != null && !password.isEmpty()) {
            secret = IotUtil.shaHMac(password, timeStamp, getClientConf().getCheckStamp());
        }

        String topic = String.format(BRIDGE_LOGIN, this.bridgeId, deviceId, requestId);
        ObjectNode jsonObject = JsonUtil.createObjectNode();
        jsonObject.putPOJO(SIGN_TYPE, BridgeSDKConstants.DEVICE_SIGN_TYPE);
        jsonObject.putPOJO(TIMESTAMP, timeStamp);
        jsonObject.putPOJO(PASSWORD, secret);

        return new RawMessage(topic, JsonUtil.convertObject2String(jsonObject));
    }

    public void logoutAsync(String deviceId, String requestId, ActionListener listener) {
        String topic = String.format(BRIDGE_LOGOUT, this.bridgeId, deviceId, requestId);
        RawMessage rawMessage = new RawMessage(topic, "");
        connection.publishMessage(rawMessage, listener);
    }

    public int logoutSync(String deviceId, int millisecondTimeout) {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String topic = String.format(BRIDGE_LOGOUT, this.bridgeId, deviceId, requestId);
        RawMessage rawMessage = new RawMessage(topic, "");
        return getSyncResult(millisecondTimeout, requestId, future, rawMessage);
    }

    public void reportProperties(String deviceId, List<ServiceProperty> properties, ActionListener listener) {
        String topic = String.format(BRIDGE_REPORT_PROPERTY, this.bridgeId, deviceId);
        ObjectNode jsonObject = JsonUtil.createObjectNode();
        jsonObject.putPOJO("services", properties);

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(jsonObject));
        connection.publishMessage(rawMessage, listener);
    }

    public void resetSecret(String deviceId, String requestId, DeviceSecret deviceSecret, ActionListener listener) {
        String topic = String.format(BRIDGE_RESET_DEVICE_SECRET, this.bridgeId, deviceId, requestId);
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceSecret));
        connection.publishMessage(rawMessage, listener);
    }

    public void reportDeviceMessage(String deviceId, DeviceMessage deviceMessage, ActionListener listener) {
        String topic = String.format(BRIDGE_REPORT_MESSAGE, this.bridgeId, deviceId);
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage));
        connection.publishMessage(rawMessage, listener);
    }

    @Override
    public void reportEvent(String deviceId, DeviceEvent event, ActionListener listener) {
        String topic = String.format(BRIDEGE_EVENT, this.bridgeId, deviceId);
        DeviceEvents events = new DeviceEvents();
        events.setServices(Collections.singletonList(event));

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(events));
        connection.publishMessage(rawMessage, listener);
    }

    public void respondCommand(String deviceId, String requestId, CommandRsp commandRsp) {
        String topic = String.format(BRIDGE_COMMAND_RESPONSE, this.bridgeId, deviceId, requestId);
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRsp));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 上报读属性响应
     *
     * @param deviceId  设备id
     * @param requestId 请求id，响应的请求id必须和请求的一致
     * @param services  服务属性
     */
    public void respondPropsGet(String deviceId, String requestId, List<ServiceProperty> services) {
        String topic = String.format(BRIDGE_PROP_GET_RESPONSE, this.bridgeId, deviceId, requestId);
        DeviceProperties deviceProperties = new DeviceProperties();
        deviceProperties.setServices(services);
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceProperties));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 上报写属性响应
     *
     * @param deviceId  设备id
     * @param requestId 请求id，响应的请求id必须和请求的一致
     * @param iotResult 写属性结果
     */
    public void respondPropsSet(String deviceId, String requestId, IotResult iotResult) {
        String topic = String.format(BRIDGE_PROP_SET_RESPONSE, this.bridgeId, deviceId, requestId);
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(iotResult));
        connection.publishMessage(rawMessage, null);
    }

}
