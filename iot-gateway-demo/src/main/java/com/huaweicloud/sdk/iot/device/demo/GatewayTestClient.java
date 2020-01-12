package com.huaweicloud.sdk.iot.device.demo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 这是一个测试工具类，用来给网关下发新增子设备的消息，用于本地测试
 */
public class GatewayTestClient {
    private static Logger log = Logger.getLogger(GatewayTestClient.class);

    public static void main(String args[]) throws InterruptedException {

        try {

            String deviceId = "test_testDevice";

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
            client.subscribe("$oc/devices/" + deviceId + "/sys/events/up");


            int requestId = 1;

            DeviceEvent deviceEvent = new DeviceEvent();
            deviceEvent.setEventType("add_sub_device_notify");
            deviceEvent.setServiceId("sub_device_manager");

            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setNodeId("subdev1");
            deviceInfo.setDeviceId("xxxxxx_subdev1");

            Map<String, Object> para = new HashMap<>();
            para.put("devices", Arrays.asList(deviceInfo));
            deviceEvent.setParas(para);

            ObjectNode node = JsonUtil.createObjectNode();
            node.putPOJO("services", Arrays.asList(deviceEvent));

            client.publish("$oc/devices/" + deviceId + "/sys/events/down",
                    new MqttMessage(JsonUtil.convertObject2String(node).getBytes()));


            while (true) {

                DeviceMessage message = new DeviceMessage("down msg");
                message.setDeviceId("xxxxxx_subdev1");

                client.publish("$oc/devices/" + deviceId + "/sys/messages/down",
                        new MqttMessage(JsonUtil.convertObject2String(message).getBytes()));
                Command command = new Command();
                command.setDeviceId("xxxxxx_subdev1");
                para = new HashMap<>();
                para.put("time", 1);
                command.setParas(para);
                command.setServiceId("smokeDetector");
                command.setCommandName("mute");

                log.info("send command :" + command.toString());

                client.publish("$oc/devices/" + deviceId + "/sys/commands/request_id=" + requestId,
                        new MqttMessage(command.toString().getBytes()));
//
//
//                requestId++;
//
//                PropsSet propsSet = new PropsSet();
//                propsSet.setDeviceId("test_testDevice");
//                ServiceData serviceData = new ServiceData();
//                serviceData.setServiceId("smokeDetector");
//                ObjectNode jsonObject = new ObjectMapper().createObjectNode();
//                jsonObject.put("alarm", requestId);
//                jsonObject.put("smokeConcentration", 23.3f);
//                serviceData.setProperties(jsonObject);
//                propsSet.setServices(Arrays.asList(serviceData));
//
//                log.info("update  properties");
//
//                client.publish("$oc/devices/" + deviceId + "/sys/properties/set/request_id=" + requestId,
//                        new MqttMessage(propsSet.toString().getBytes()));
//
//
//                requestId++;
//                Thread.sleep(1000);
//
//                PropsGet propsGet = new PropsGet();
//                propsGet.setDeviceId("test_testDevice");
//                propsGet.setServiceId("smokeDetector");
//
//                log.info("get  properties");
//
//                client.publish("$oc/devices/" + deviceId + "/sys/properties/get/request_id=" + requestId,
//                        new MqttMessage(propsGet.toString().getBytes()));


                Thread.sleep(10000);
                requestId++;

            }


        } catch (MqttException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return;
        }

    }


}
