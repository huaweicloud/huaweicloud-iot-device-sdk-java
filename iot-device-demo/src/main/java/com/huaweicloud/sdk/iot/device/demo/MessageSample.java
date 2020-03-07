package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.log4j.Logger;


/**
 * 演示如何直接使用DeviceClient进行消息透传
 */
public class MessageSample {

    private static Logger log = Logger.getLogger(MessageSample.class);


    public static void main(String[] args) throws InterruptedException {


        //创建设备
        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "secret");
        if (device.init() != 0) {
            return;

        }

        //接收平台下行消息
        device.getClient().setDeviceMessageListener(new DeviceMessageListener() {
            @Override
            public void onDeviceMessage(DeviceMessage deviceMessage) {
                log.info("onDeviceMessage:" + deviceMessage.toString());
            }
        });


        while (true) {

            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.error("reportDeviceMessage fail: " + var2);
                }
            });

            Thread.sleep(10000);
        }
    }
}
