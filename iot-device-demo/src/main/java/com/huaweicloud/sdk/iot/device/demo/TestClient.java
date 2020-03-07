package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.client.requests.*;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 这是一个mqtt本地测试工具，用于在不连接平台的情况下进行设备侧代码的模拟测试，使用方法：
 * 1、本地安装并启动mosquitto
 * 2、修改设备侧代码，设置serverUri为本地mosquitto地址，启动设备侧代码
 * 3、启动本测试客户端，它会周期性的向设备下发命令、属性操作
 */
public class TestClient {
    private static Logger log = Logger.getLogger(TestClient.class);

    public static void main(String args[]) throws InterruptedException {

        try {

            String deviceId = "5e06bfee334dd4f33759f5b3_demo";

            MqttClient client = new MqttClient("tcp://localhost:1883", "123", new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setAutomaticReconnect(true);
            client.connect(options);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    log.info("connectionLost");
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    log.info("messageArrived , topic " + s + ", msg = " + mqttMessage.toString());

                    if (s.contains("sys/messages/up")) {

                        client.publish(s.replace("up", "down"),
                                new MqttMessage(mqttMessage.getPayload()));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    log.info("deliveryComplete ");
                }
            });

            client.subscribe("$oc/devices/+/sys/messages/up");
            client.subscribe("$oc/devices/" + deviceId + "/sys/commands/response/+");
            client.subscribe("$oc/devices/" + deviceId + "/sys/properties/get/response/+");
            client.subscribe("$oc/devices/" + deviceId + "/sys/properties/set/response/+");
            client.subscribe("$oc/devices/" + deviceId + "/sys/properties/report");
            client.subscribe("$oc/devices/" + deviceId + "/sys/desired/properties/get/+");


            int requestId = 1;
            while (true) {

                DeviceMessage message = new DeviceMessage("down msg");
                message.setId("id");
                message.setName("name");

                client.publish("$oc/devices/" + deviceId + "/sys/messages/down",
                        new MqttMessage(JsonUtil.convertObject2String(message).getBytes()));
                DeviceCommand command = new DeviceCommand();
                Map<String, Object> para = new HashMap<>();
                para.put("duration", 20);
                command.setParas(para);
                command.setServiceId("smokeDetector");
                command.setCommandName("ringAlarm");

                log.info("send command");

                client.publish("$oc/devices/" + deviceId + "/sys/commands/request_id=" + requestId,
                        new MqttMessage(command.toString().getBytes()));

                requestId++;

                PropsSet propsSet = new PropsSet();
                ServiceProperty serviceData = new ServiceProperty();
                serviceData.setServiceId("smokeDetector");
                Map<String, Object> json = new HashMap<>();
                json.put("alarm", 0);
                //json.put("humidity", 1.1f);
                serviceData.setProperties(json);
                propsSet.setServices(Arrays.asList(serviceData));

                log.info("update  properties");

                client.publish("$oc/devices/" + deviceId + "/sys/properties/set/request_id=" + requestId,
                        new MqttMessage(propsSet.toString().getBytes()));

                requestId++;
                Thread.sleep(1000);

                PropsGet propsGet = new PropsGet();
                propsGet.setServiceId("smokeDetector");

                log.info("get  properties");

                client.publish("$oc/devices/" + deviceId + "/sys/properties/get/request_id=" + requestId,
                        new MqttMessage(propsGet.toString().getBytes()));


                Thread.sleep(10000);
                requestId++;

            }


        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return;
        }

    }


}
