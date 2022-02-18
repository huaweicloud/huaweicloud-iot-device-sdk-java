package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 演示如何直接使用DeviceClient进行设备属性的上报和读写
 */
public class PropertySample {
    private static final Logger log = LogManager.getLogger(PropertySample.class);

    public static void main(String[] args) throws InterruptedException {

        // 加载iot平台的ca证书，进行服务端校验
        URL resource = PropertySample.class.getClassLoader().getResource("ca.jks");
        File file = new File(resource.getPath());

        // 创建设备并初始化
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            "5e06bfee334dd4f33759f5b3_demo", "mysecret", file);
        if (device.init() != 0) {
            return;
        }

        // 接收平台下发的属性读写
        device.getClient().setPropertyListener(new PropertyListener() {

            // 处理写属性
            @Override
            public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

                // 遍历service
                for (ServiceProperty serviceProperty : services) {

                    log.info("OnPropertiesSet, serviceId is {}", serviceProperty.getServiceId());

                    // 遍历属性
                    for (String name : serviceProperty.getProperties().keySet()) {
                        log.info("property name is {}", name);
                        log.info("set property value is {}", serviceProperty.getProperties().get(name));
                    }

                }
                // 修改本地的属性值
                device.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
            }

            /**
             * 处理读属性。多数场景下，用户可以直接从平台读设备影子，此接口不用实现。
             * 但如果需要支持从设备实时读属性，则需要实现此接口。
             */
            @Override
            public void onPropertiesGet(String requestId, String serviceId) {

                log.info("OnPropertiesGet, the serviceId is {}", serviceId);
                Map<String, Object> json = new HashMap<>();
                Random rand = new SecureRandom();
                json.put("alarm", 1);
                json.put("temperature", rand.nextFloat() * 100.0f);
                json.put("humidity", rand.nextFloat() * 100.0f);
                json.put("smokeConcentration", rand.nextFloat() * 100.0f);

                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setProperties(json);
                serviceProperty.setServiceId("smokeDetector");

                device.getClient().respondPropsGet(requestId, Arrays.asList(serviceProperty));
            }
        });

        // 定时上报属性
        while (true) {

            Map<String, Object> json = new HashMap<>();
            Random rand = new SecureRandom();

            //按照物模型设置属性
            json.put("alarm", 1);
            json.put("temperature", rand.nextFloat() * 100.0f);
            json.put("humidity", rand.nextFloat() * 100.0f);
            json.put("smokeConcentration", rand.nextFloat() * 100.0f);

            ServiceProperty serviceProperty = new ServiceProperty();
            serviceProperty.setProperties(json);
            serviceProperty.setServiceId("smokeDetector");//serviceId要和物模型一致

            device.getClient().reportProperties(Arrays.asList(serviceProperty), new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    log.info("pubMessage success");
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.error("reportProperties failed" + var2.toString());
                }
            });

            Thread.sleep(10000);
        }
    }
}
