package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.demo.utils.CertificateUtil;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 此例演示使用证书认证
 */
public class X509CertificateDeviceSample {

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws Exception {
        // windows下读取pem格式证书demo
        KeyStore keyStore = CertificateUtil.getKeyStore("D:\\SDK\\cert\\deviceCert.pem",
            "D:\\SDK\\cert\\deviceCert.key", "");

        /**
         * 读取keystore格式证书
         *
         * KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         * keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "huawei".toCharArray());
         *
         */
        /**
         * 读取国密双证书
         * KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         * keyStore.load(null, null);
         * GmCertificate gmSignCert = new GmCertificate("gm-sig-certificate", "D:\\devicecert\\gmcert_s\\CS.cert.pem", "gm-sig-private-key", "D:\\devicecert\\gmcert_s\\CS.key.pem", "");
         * GmCertificate gmEncCert = new GmCertificate("gm-enc-certificate", "D:\\devicecert\\gmcert_e\\CE.cert.pem", "gm-enc-private-key", "D:\\devicecert\\gmcert_e\\CE.key.pem", "");
         * if(!CertificateUtil.getGmKeyStore(keyStore, gmSignCert) || !CertificateUtil.getGmKeyStore(keyStore, gmEncCert)) {
         * return;
         * }
         */

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 使用证书创建设备
        IoTDevice iotDevice = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            "5e06bfee334dd4f33759f5b3_demo3", keyStore, "", tmpCAFile);

        if (iotDevice.init() != 0) {
            return;
        }

        // 定时上报属性
        while (true) {

            Map<String, Object> json = new HashMap<>();
            Random rand = new Random();

            // 按照物模型设置属性
            json.put("alarm", 0);
            json.put("temperature", rand.nextFloat() * 100.0f);
            json.put("humidity", rand.nextFloat() * 100.0f);
            json.put("smokeConcentration", rand.nextFloat() * 100.0f);

            ServiceProperty serviceProperty = new ServiceProperty();
            serviceProperty.setProperties(json);
            serviceProperty.setServiceId("smokeDetector"); // serviceId要和物模型一致

            iotDevice.getClient().reportProperties(Arrays.asList(serviceProperty), new ActionListener() {
                @Override
                public void onSuccess(Object context) {

                }

                @Override
                public void onFailure(Object context, Throwable var2) {

                }
            });

            Thread.sleep(10000);
        }
    }
}
