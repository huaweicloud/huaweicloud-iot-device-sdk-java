package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.log4j.Logger;

/**
 * 演示设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootstrapSample {

    public static final Logger log = Logger.getLogger(BootstrapSample.class);

    public static void main(String args[]) {

        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String secret = "mysecret";
        String bootstrapUri = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

        //创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(bootstrapUri, deviceId, secret);
        bootstrapClient.bootstrap(new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                String address = (String) context;
                log.info("bootstrap success:" + address);

                //引导成功后关闭客户端
                bootstrapClient.close();
                IoTDevice device = new IoTDevice("ssl://" + address, deviceId, secret);
                if (device.init() != 0) {
                    return;

                }
                device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
            }

            @Override
            public void onFailure(Object context, Throwable var2) {

                log.error("bootstrap failed:" + var2.getMessage());
            }
        });

    }
}
