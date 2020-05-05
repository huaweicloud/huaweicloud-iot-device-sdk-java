package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.log4j.Logger;

import java.security.KeyStore;

/**
 * 演示自注册场景，设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootsrapSelfRegSample {
    private static final Logger log = Logger.getLogger(BootsrapSelfRegSample.class);

    public static void main(String[] args) throws Exception {

        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String scopeId = "myScopeId";
        String bootstrapUri = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";


        //读取pem格式证书
        KeyStore keyStore = X509CertificateDeviceSample.getKeyStore("D:\\SDK\\cert\\deviceCert.pem", "D:\\SDK\\cert\\deviceCert.key", "");

        //读取keystore格式证书
//        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "huawei".toCharArray());

        //创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(bootstrapUri, deviceId, keyStore, "yourPassWord", scopeId);
        bootstrapClient.bootstrap(new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                String address = (String) context;
                log.info("bootstrap success:" + address);

                //引导成功后关闭客户端
                bootstrapClient.close();
                IoTDevice device = new IoTDevice("ssl://" + address, deviceId, keyStore, "yourPassWord");
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
