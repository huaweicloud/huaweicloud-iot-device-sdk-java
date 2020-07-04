package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.CommandV3Listener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRspV3;
import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;
import com.huaweicloud.sdk.iot.device.client.requests.DevicePropertiesV3;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceData;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * 演示如何使用旧接口（调用V3接口）数据上报/命令下发，一般用不到
 *
 */
public class PropertyV3AndCmdV3Sample {

    private static final Logger log = Logger.getLogger(PropertyV3AndCmdV3Sample.class);

    public static void main(String[] args) {

        String deviceId = "5e06bfee334dd4f33759f5b3_demo";
        String secret = "mysecret";

        //创建设备并初始化
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                deviceId, secret);
        if (device.init() != 0) {
            return;
        }

        DevicePropertiesV3 devicePropertiesV3 = new DevicePropertiesV3();
        devicePropertiesV3.setMsgType("deviceReq");

        ServiceData serviceData = new ServiceData();
        serviceData.setServiceId("analog");

        Map<String, Object> json1 = new HashMap<>();
        Random rand = new Random();

        json1.put("alarm", 1);
        json1.put("temperature", rand.nextFloat() * 100.0f);
        json1.put("humidity", rand.nextFloat() * 100.0f);
        json1.put("smokeConcentration", rand.nextFloat() * 100.0f);
        serviceData.setServiceData(json1);

        List<ServiceData> list = new ArrayList<>();
        list.add(serviceData);
        devicePropertiesV3.setServiceDatas(list);

        System.out.println(devicePropertiesV3.toString());


        //订阅V3 TOPIC
        device.getClient().subscribeTopic("/huawei/v1/devices/" + deviceId + "/command/json", new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("subscribe success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("subscribe failed");
            }
        });

        //通过V3接口上报属性
        device.getClient().reportPropertiesV3(devicePropertiesV3, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("pubMessage success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportProperties failed" + var2.toString());
            }
        });

        //设置监听器监听V3接口下发的命令
        device.getClient().setCommandV3Listener(new CommandV3Listener() {
            @Override
            public void onCommandV3(CommandV3 commandV3) {
                log.info("onCommand, msgType = " + commandV3.getMsgType());
                log.info("onCommand, serviceId = " + commandV3.getServiceId());
                log.info("onCommand, cmd = " + commandV3.getCmd());
                log.info("onCommand, mid = " + commandV3.getMid());
                log.info("onCommand, paras = " + commandV3.getParas().toString());

                //处理命令

                //上报命令响应
                CommandRspV3 commandRspV3 = new CommandRspV3("deviceRsp", commandV3.getMid(), 0);
                Map<String, Object> json = new HashMap<>();
                json.put("result", 0);
                commandRspV3.setBody(json);

                device.getClient().responseCommandV3(commandRspV3, new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {
                        log.info("responseCommandV3 success");
                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("responseCommandV3 failed" + var2.toString());
                    }
                });
            }
        });

    }

}
