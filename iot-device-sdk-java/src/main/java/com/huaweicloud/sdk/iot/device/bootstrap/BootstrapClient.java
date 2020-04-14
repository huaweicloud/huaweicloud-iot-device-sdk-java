package com.huaweicloud.sdk.iot.device.bootstrap;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.transport.mqtt.MqttConnection;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 引导客户端，用于设备引导来获取服务端地址
 */
public class BootstrapClient implements RawMessageListener {

    private String deviceId;
    private Connection connection;
    private ActionListener listener;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final Logger log = Logger.getLogger(BootstrapClient.class);

    /**
     * 构造函数，使用密码创建
     *
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     */
    public BootstrapClient(String bootstrapUri, String deviceId, String deviceSecret) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(bootstrapUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setSecret(deviceSecret);
        this.deviceId = deviceId;
        this.connection = new MqttConnection(clientConf, this);
        log.info("create BootstrapClient: " + clientConf.getDeviceId());

    }

    /**
     * 构造函数，使用证书创建
     *
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param keyStore     证书容器
     * @param keyPassword  证书密码
     */
    public BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(bootstrapUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setKeyPassword(keyPassword);
        clientConf.setKeyStore(keyStore);
        this.deviceId = deviceId;
        this.connection = new MqttConnection(clientConf, this);
        log.info("create BootstrapClient: " + clientConf.getDeviceId());
    }

    @Override
    public void onMessageReceived(RawMessage message) {

        if (message.getTopic().contains("/iodpsCommand")) {
            ObjectNode node = JsonUtil.convertJsonStringToObject(message.toString(), ObjectNode.class);
            String address = node.get("address").asText();
            log.info("bootstrap ok address:" + address);

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(address);
                }
            });
        }
    }

    /**
     * 发起设备引导
     *
     * @param listener 监听器用来接收引导结果
     * @throws IllegalArgumentException 参数非法异常
     */
    public void bootstrap(ActionListener listener) throws IllegalArgumentException {

        this.listener = listener;

        if (connection.connect() != 0) {
            log.error("connect failed");
            listener.onFailure(null, new Exception("connect failed"));
            return;
        }

        String bsTopic = "/huawei/v1/devices/" + this.deviceId + "/iodpsCommand";
        connection.subscribeTopic(bsTopic, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("subscribeTopic failed:" + bsTopic);
                listener.onFailure(context, var2);

            }
        });

        String topic = "/huawei/v1/devices/" + this.deviceId + "/iodpsData";
        RawMessage rawMessage = new RawMessage(topic, "");

        connection.publishMessage(rawMessage, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {

                listener.onFailure(context, var2);
            }
        });


    }

    /**
     * 关闭客户端
     */
    public void close() {
        connection.close();
    }
}
