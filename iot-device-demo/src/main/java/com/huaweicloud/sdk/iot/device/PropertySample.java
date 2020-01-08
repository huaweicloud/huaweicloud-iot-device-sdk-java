package com.huaweicloud.sdk.iot.device;


import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * 演示如何直接使用DeviceClient进行设备属性的上报和读写
 */
public class PropertySample {

    public static int alarm = 1; //本地保存属性值
    private static Logger log = Logger.getLogger(PropertySample.class);

    public static void main(String[] args) throws InterruptedException {


        //创建设备并初始化
        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret");
        if (device.init() != 0) {
            return;
        }

        //接收平台下发的属性读写
        device.getClient().setPropertyListener(new PropertyListener() {

            //处理写属性
            @Override
            public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

                //遍历service
                for (ServiceProperty serviceProperty : services) {

                    log.info("OnPropertiesSet, serviceId =  " + serviceProperty.getServiceId());

                    //遍历属性
                    for (String name : serviceProperty.getProperties().keySet()) {
                        log.info("property name = " + name);
                        log.info("set property value = " + serviceProperty.getProperties().get(name));
                        if (name.equals("alarm")) {
                            //修改本地值
                            alarm = (int) serviceProperty.getProperties().get(name);
                        }
                    }

                }
                //修改本地的属性值
                device.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
            }

            //处理读属性
            @Override
            public void onPropertiesGet(String requestId, String serviceId) {

                log.info("OnPropertiesGet " + serviceId);
                Map<String, Object> json = new HashMap<>();
                Random rand = new Random();
                json.put("alarm", alarm);
                json.put("temperature", rand.nextFloat() * 100.0f);
                json.put("humidity", rand.nextFloat() * 100.0f);
                json.put("smokeConcentration", rand.nextFloat() * 100.0f);

                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setProperties(json);
                serviceProperty.setServiceId("smokeDetector");

                device.getClient().respondPropsGet(requestId, Arrays.asList(serviceProperty));
            }
        });

        //定时上报属性
        while (true) {

            Map<String, Object> json = new HashMap<>();
            Random rand = new Random();

            //按照物模型设置属性
            json.put("alarm", alarm);
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
