package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.demo.utils.CertificateUtil;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.security.KeyStore;

/**
 * 演示自注册场景（云证书方式），设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootsrapSelfRegCcmSample {
    private static final Logger log = LogManager.getLogger(BootsrapSelfRegSample.class);

    /**
     * 设备ID（自注册场景下，设备ID无需在IoT平台上注册）
     */
    private static String deviceId = "[Please input your device id here, example:702b1038-a174-4a1d-969f-f67f8df43c4a]";

    /**
     * 设备证书信息
     */
    private static String DEVICE_CERT = "[Please input your device cert path here, example:D:\\SDK\\cert\\deviceCert.pem]";
    private static String DEVICE_CERT_KEY = "[Please input your device cert key path here, example:D:\\SDK\\cert\\deviceCert.key]";
    private static String DEVICE_CERT_KEY_PWD = "[Please input your device cert key pwd here, example:yourpwd. If not set, input empty string]";

    /**
     * 设备发放设备侧引导地址（无需修改）
     */
    private static final String BOOTSTRAP_URI = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

    /**
     * IoT平台CA证书
     */
    private static final String IODTA_ROOT_CA = "ca.jks";

    public static void main(String[] args) throws Exception {
        // 读取pem格式设备证书
        KeyStore keyStore = CertificateUtil.getKeyStore(DEVICE_CERT, DEVICE_CERT_KEY, DEVICE_CERT_KEY_PWD);

        // 加载iot平台（设备接入）的ca证书，进行服务端校验
        URL resource = BootsrapSelfRegSample.class.getClassLoader().getResource(IODTA_ROOT_CA);
        File file = new File(resource.getPath());

        // 创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(BOOTSTRAP_URI, deviceId, keyStore, DEVICE_CERT_KEY_PWD);
        bootstrapClient.bootstrap(new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                // 引导成功，获取到iot平台的地址
                String address = (String) context;
                log.info("bootstrap success:" + address);

                // 引导成功后关闭客户端
                bootstrapClient.close();

                // 与iot平台建立连接，上报消息
                IoTDevice device = new IoTDevice("ssl://" + address, deviceId, keyStore, DEVICE_CERT_KEY_PWD, file);
                if (device.init() != 0) {
                    return;
                }
                device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
            }

            @Override
            public void onFailure(Object context, Throwable throwable) {
                // 引导失败
                log.error("bootstrap failed:" + throwable.getMessage());
            }
        });
    }
}
