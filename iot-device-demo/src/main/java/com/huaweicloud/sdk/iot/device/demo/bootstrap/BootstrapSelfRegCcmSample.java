package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.demo.utils.CertificateUtil;

import java.security.KeyStore;

/**
 * 演示自注册场景（云证书方式），设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootstrapSelfRegCcmSample extends BaseBootstrapSample {
    /**
     * 设备ID（自注册场景下，设备ID无需在IoT平台上注册）
     */
    private static final String deviceId = "[Please input your device id here, example:702b1038-a174-4a1d-969f-f67f8df43c4a]";

    /**
     * 设备证书信息
     */
    private static final String DEVICE_CERT
        = "[Please input your device cert path here, example:D:\\SDK\\cert\\deviceCert.pem]";

    private static final String DEVICE_CERT_KEY
        = "[Please input your device cert key path here, example:D:\\SDK\\cert\\deviceCert.key]";

    private static final String DEVICE_CERT_KEY_PWD
        = "[Please input your device cert key pwd here, example:yourpwd. If not set, input empty string]";

    public static void main(String[] args) throws Exception {
        // 读取pem格式设备证书
        KeyStore keyStore = CertificateUtil.getKeyStore(DEVICE_CERT, DEVICE_CERT_KEY, DEVICE_CERT_KEY_PWD);

        // 创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(BOOTSTRAP_URI, deviceId, keyStore, DEVICE_CERT_KEY_PWD, PLATFORM_CA_PROVIDER);
        SimpleBootstrapActionListener defaultActionListener = new SimpleBootstrapActionListener(bootstrapClient);
        bootstrapClient.bootstrap(defaultActionListener);
    }
}
