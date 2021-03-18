package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;


/**
 * 演示设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootstrapSample {

    public static void main(String[] args) {

        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String secret = "mysecret";
        String bootstrapUri = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

        //创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(bootstrapUri, deviceId, secret);
        DefaultBootstrapActionListener defaultBootstrapActionListener = new DefaultBootstrapActionListener(deviceId,
            secret, bootstrapClient, bootstrapUri);
        bootstrapClient.bootstrap(defaultBootstrapActionListener);

    }
}
