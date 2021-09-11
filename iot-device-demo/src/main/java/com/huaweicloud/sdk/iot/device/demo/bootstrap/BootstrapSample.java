package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;

/**
 * 演示密钥认证设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootstrapSample {
    /**
     * 设备发放设备侧引导地址（无需修改）
     */
    private static final String BOOTSTRAP_URI = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

    /**
     * 设备ID（设备ID需在设备发放上注册）
     */
    private static String deviceId = "[Please input your device id here, example:702b1038-a174-4a1d-969f-f67f8df43c4a]";;
    
    /**
     * 设备密钥
     */
    private static String secret = "[Please input your device secret here, example:mysecret]";

    public static void main(String[] args) {
        // 创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(BOOTSTRAP_URI, deviceId, secret);
        DefaultBootstrapActionListener defaultBootstrapActionListener = new DefaultBootstrapActionListener(deviceId,
            secret, bootstrapClient, BOOTSTRAP_URI);
        bootstrapClient.bootstrap(defaultBootstrapActionListener);
    }
}
