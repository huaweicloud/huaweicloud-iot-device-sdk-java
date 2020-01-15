package com.huaweicloud.sdk.iot.device.bootstrap;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.transport.Transport;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

/**
 * 引导客户端，用于设备引导来获取服务端地址
 */
public class BootstrapClient implements RawMessageListener {

    private ClientConf clientConf;
    private Transport transport;
    private ActionListener listener;

    private static final Logger log = Logger.getLogger(BootstrapClient.class);


    public BootstrapClient(ClientConf clientConf) {
        this.clientConf = clientConf;
        transport = new Transport(clientConf);
        transport.setMessageListener(this);
    }

    @Override
    public void onMessageReceived(RawMessage message) {

        if (message.getTopic().contains("/iodpsCommand")) {
            ObjectNode node = JsonUtil.convertJsonStringToObject(message.toString(), ObjectNode.class);
            String address = node.get("address").asText();
            log.info("bootstrap ok address:" + address);
            listener.onSuccess(address);
        }
    }

    private void checkClientConf(ClientConf clientConf) throws IllegalArgumentException {
        if (clientConf == null) {
            throw new IllegalArgumentException("clientConf is null");
        }
        if (clientConf.getDeviceId() == null || clientConf.getDeviceId().isEmpty()) {
            throw new IllegalArgumentException("clientConf.getDeviceId() is null");
        }
        if (clientConf.getSecret() == null || clientConf.getSecret().isEmpty()) {
            throw new IllegalArgumentException("clientConf.getSecret() is null");
        }
        if (clientConf.getBootstrapUri() == null || clientConf.getBootstrapUri().isEmpty()) {
            throw new IllegalArgumentException("clientConf.getBootstrapUri() is null");
        }

    }

    /**
     * 发起设备引导
     *
     * @param listener 监听器用来接收引导结果
     * @throws IllegalArgumentException
     */
    public void bootstrap(ActionListener listener) throws IllegalArgumentException {

        checkClientConf(clientConf);
        this.listener = listener;

        if (transport.connect() != 0) {
            log.error("connect failed");
            listener.onFailure(null, new Exception("connect failed"));
            return;
        }

        String bsTopic = "/huawei/v1/devices/" + clientConf.getDeviceId() + "/iodpsCommand";
        transport.subscribeTopic(bsTopic, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("subscribeTopic failed:" + bsTopic);
                listener.onFailure(context, var2);

            }
        });

        String topic = "/huawei/v1/devices/" + clientConf.getDeviceId() + "/iodpsData";
        RawMessage rawMessage = new RawMessage(topic, "");

        transport.publishMsg(rawMessage, new ActionListener() {
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
        transport.close();
    }
}
