package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 演示如何直接使用DeviceClient进行消息透传
 */
public class MessageSample {

    private static final Logger log = LogManager.getLogger(BootsrapSelfRegSample.class);

    public static void main(String[] args) throws InterruptedException {

        //创建设备
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "secret");
        if (device.init() != 0) {
            return;

        }

        //接收平台下行消息
        device.getClient().setDeviceMessageListener(
            deviceMessage -> log.info("onDeviceMessage:" + deviceMessage.toString()));

        while (true) {

            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    log.info("reportDeviceMessage ok");
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.error("reportDeviceMessage fail: " + var2);
                }
            });

            //上报自定义topic消息，注意需要先在平台配置自定义topic
            String topic = "$oc/devices/" + device.getDeviceId() + "/user/wpy";
            device.getClient().publishRawMessage(new RawMessage(topic, "hello raw message "),
                new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {
                        log.info("publishRawMessage ok: ");
                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("publishRawMessage fail: " + var2);
                    }
                });

            Thread.sleep(5000);
        }
    }
}
