package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReceiveBootstrapMsgSample {

    private static final Logger log = LogManager.getLogger(ReceiveBootstrapMsgSample.class);

    public static void main(String[] args) {

        String bootstrapUri = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "deviceId";
        String secret = "yourSecret";

        //创建设备
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-5.myhuaweicloud.com:8883",
            deviceId, secret);
        if (device.init() != 0) {
            return;

        }

        //接收平台下行消息
        device.getClient().setDeviceMessageListener(deviceMessage -> {
            log.info("onDeviceMessage:" + deviceMessage.toString());

            //BootstrapRequestTrigger是平台系统字段，如果收到此字段，设备侧需要发起引导。
            if ("BootstrapRequestTrigger".equals(deviceMessage.getContent())) {

                bootstrap(bootstrapUri, deviceId, secret);

            }

        });

    }

    private static void bootstrap(String bootstrapUri, String deviceId, String secret) {
        //创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(bootstrapUri, deviceId, secret);
        DefaultBootstrapActionListener defaultBootstrapActionListener = new DefaultBootstrapActionListener(deviceId,
            secret, bootstrapClient);
        bootstrapClient.bootstrap(defaultBootstrapActionListener);
    }

}
