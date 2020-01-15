package com.huaweicloud.sdk.iot.device.transport.mqtt;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.net.ssl.SSLContext;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * mqtt连接
 */
public class MqttConnection implements Connection {

    private static final int DEFAULT_QOS = 1;
    private static final int DEFAULT_CONNECT_TIMEOUT = 60;
    private static final int DEFAULT_KEEPLIVE = 120;
    private static final String connectType = "0";
    private static final String checkTimestamp = "0";
    private static final Logger log = Logger.getLogger(MqttConnection.class);
    private ClientConf clientConf;
    private boolean connectFinished = false;
    private String connectionId = null;
    private MqttAsyncClient client;
    private ConnectListener connectListener;
    private RawMessageListener rawMessageListener;
    private MqttCallback callback = new MqttCallbackExtended() {

        @Override
        public void connectionLost(Throwable cause) {
            log.error("Connection lost.", cause);
            if (connectListener != null) {
                connectListener.connectionLost(cause);
            }

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            log.info("messageArrived topic =  " + topic + ", msg = " + message.toString());
            RawMessage rawMessage = new RawMessage(topic, message.toString());
            try {
                if (rawMessageListener != null) {
                    rawMessageListener.onMessageReceived(rawMessage);
                }

            } catch (Exception e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }

        }


        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            log.info("Mqtt client connected. address :" + serverURI);

            subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/messages/down", null);
            subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/commands/#", null);
            subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/set/#", null);
            subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/get/#", null);
            subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/shadow/get/response/#", null);
            subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/events/down", null);

            if (connectListener != null) {
                connectListener.connectComplete(reconnect, serverURI);
            }

        }

    };

    public MqttConnection(ClientConf clientConf) {
        this.clientConf = clientConf;

    }

    @Override
    public int connect() {

        try {

            String timeStamp = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
            String clientId = clientConf.getDeviceId() + "_" + connectType + "_" + checkTimestamp + "_" + timeStamp;

            try {
                client = new MqttAsyncClient(clientConf.getServerUri(), clientId);
            } catch (MqttException e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }

            DisconnectedBufferOptions bufferOptions = new DisconnectedBufferOptions();
            bufferOptions.setBufferEnabled(true);
            if (clientConf.getBufferSize() != null) {
                bufferOptions.setBufferSize(clientConf.getBufferSize());
            }

            client.setBufferOpts(bufferOptions);

            MqttConnectOptions options = new MqttConnectOptions();
            if (clientConf.getServerUri().contains("ssl:")) {

                try {
                    SSLContext sslContext = IotUtil.getSSLContext(clientConf);
                    options.setSocketFactory(sslContext.getSocketFactory());
                } catch (Exception e) {
                    log.error(ExceptionUtil.getBriefStackTrace(e));
                    return -1;
                }
            }

            options.setCleanSession(false);
            options.setUserName(clientConf.getDeviceId());

            if (clientConf.getSecret() != null && !clientConf.getSecret().isEmpty()) {
                String passWord = IotUtil.sha256_mac(clientConf.getSecret(), timeStamp);
                options.setPassword(passWord.toCharArray());
            }

            options.setConnectionTimeout(DEFAULT_CONNECT_TIMEOUT);
            options.setKeepAliveInterval(DEFAULT_KEEPLIVE);
            options.setAutomaticReconnect(true);
            client.setCallback(callback);

            log.info("try to connect to " + clientConf.getServerUri());


            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {

                    log.info("connect success " + clientConf.getServerUri());

                    synchronized (MqttConnection.this) {
                        connectFinished = true;
                        MqttConnection.this.notifyAll();
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    log.info("connect failed " + throwable.toString());

                    synchronized (MqttConnection.this) {
                        connectFinished = true;
                        MqttConnection.this.notifyAll();
                    }
                }
            });

            synchronized (this) {

                while (!connectFinished) {

                    try {
                        wait(DEFAULT_CONNECT_TIMEOUT * 1000);
                    } catch (InterruptedException e) {
                        log.error(ExceptionUtil.getBriefStackTrace(e));
                    }
                }
            }


        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));

        }

        return client.isConnected() ? 0 : -1;
    }

    @Override
    public void publishMessage(RawMessage message, ActionListener listener) {

        try {
            MqttMessage mqttMessage = new MqttMessage(message.getPayload());
            int qos = clientConf.getQos() == 0 ? 0 : DEFAULT_QOS;
            mqttMessage.setQos(qos);

            client.publish(message.getTopic(), mqttMessage, message.getTopic(), new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    if (listener != null) {
                        listener.onSuccess(null);
                    }

                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    log.error("publish message failed   " + message);
                    if (listener != null) {
                        listener.onFailure(null, throwable);
                    }

                }
            });
            log.info("publish message topic =  " + message.getTopic() + ", msg = " + message.toString());
        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    public void close() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }
    }

    @Override
    public boolean isConnected() {
        if (client == null) {
            return false;
        }
        return client.isConnected();
    }

    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    public void setRawMessageListener(RawMessageListener rawMessageListener) {
        this.rawMessageListener = rawMessageListener;
    }


    /**
     * 订阅指定主题
     *
     * @param topic 主题
     */
    public void subscribeTopic(String topic, ActionListener listener) {

        int qos = clientConf.getQos() == 0 ? 0 : DEFAULT_QOS;

        try {
            client.subscribe(topic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {

                    if (listener != null) {
                        listener.onSuccess(topic);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    log.error("subscribe failed:" + topic);
                    if (listener != null) {
                        listener.onFailure(topic, throwable);
                    }
                }
            });
        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            if (listener != null) {
                listener.onFailure(topic, e);
            }
        }

    }


}
