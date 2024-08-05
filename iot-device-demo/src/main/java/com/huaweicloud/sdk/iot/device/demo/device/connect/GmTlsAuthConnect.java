package com.huaweicloud.sdk.iot.device.demo.device.connect;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.demo.device.MessageSample;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/*
 * 若要使用国密，需要在pom.xml中增加bgmprovider引用，详情可见README.
 */
public class GmTlsAuthConnect {
    private static final Logger log = LogManager.getLogger(MessageSample.class);

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());;
        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = GmTlsAuthConnect.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        /**
         * 读取keystore格式证书
         * // keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         * // keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "huawei".toCharArray());
         */

        /**
         * 读取国密双证书
         *
         * keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         * keyStore.load(null, null);
         * GmCertificate gmSignCert = new GmCertificate("gm-sig-certificate", "D:\\devicecert\\gmcert_s\\CS.cert.pem", "gm-sig-private-key", "D:\\devicecert\\gmcert_s\\CS.key.pem", "");
         * GmCertificate gmEncCert = new GmCertificate("gm-enc-certificate", "D:\\devicecert\\gmcert_e\\CE.cert.pem", "gm-enc-private-key", "D:\\devicecert\\gmcert_e\\CE.key.pem", "");
         * if(!CertificateUtil.getGmKeyStore(keyStore, gmSignCert) || !CertificateUtil.getGmKeyStore(keyStore, gmEncCert)) {
         *  return;
         * }
         */
        // 使用证书创建设备
        IoTDevice device = new IoTDevice("ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo3", keyStore, "", tmpCAFile);
        if (device == null) {
            return;
        }
        // 使用国密通信
        device.getClient().getClientConf().setGmssl(true);

        if (device.init() != 0) {
            return;
        }
        // 定时上报属性
        int i = 0;
        while (i++ < 100) {
            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    log.info("reportDeviceMessage ok");
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.error("reportDeviceMessage fail: " + var2);
                }
            });
            Thread.sleep(10000);
        }
    }
}

