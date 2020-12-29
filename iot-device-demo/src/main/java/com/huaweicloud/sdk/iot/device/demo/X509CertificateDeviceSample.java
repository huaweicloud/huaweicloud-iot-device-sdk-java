package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 此例演示使用证书认证
 */
public class X509CertificateDeviceSample {

    private static final Logger log = LogManager.getLogger(X509CertificateDeviceSample.class);

    public static void main(String[] args) throws Exception {

        //读取pem格式证书
        KeyStore keyStore = getKeyStore("D:\\SDK\\cert\\deviceCert.pem", "D:\\SDK\\cert\\deviceCert.key", "");

        /**
         * 读取keystore格式证书
         *
         * KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         * keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "huawei".toCharArray());
         *
         */

        //使用证书创建设备
        IoTDevice iotDevice = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            "5e06bfee334dd4f33759f5b3_demo3", keyStore, "");

        if (iotDevice.init() != 0) {
            return;
        }

        //定时上报属性
        while (true) {

            Map<String, Object> json = new HashMap<>();
            Random rand = new Random();

            //按照物模型设置属性
            json.put("alarm", 0);
            json.put("temperature", rand.nextFloat() * 100.0f);
            json.put("humidity", rand.nextFloat() * 100.0f);
            json.put("smokeConcentration", rand.nextFloat() * 100.0f);

            ServiceProperty serviceProperty = new ServiceProperty();
            serviceProperty.setProperties(json);
            serviceProperty.setServiceId("smokeDetector");//serviceId要和物模型一致

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

    public static KeyStore getKeyStore(String certificateFile, String privateKeyFile, String keyPassword)
        throws Exception {
        if (certificateFile == null || privateKeyFile == null) {
            log.error("input null");
            return null;
        }
        if (keyPassword == null) {
            keyPassword = "";
        }

        Certificate cert = null;
        try (FileInputStream inputStream = new FileInputStream(certificateFile)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(inputStream);
        }

        KeyPair keyPair = null;
        try (FileInputStream keyInput = new FileInputStream(privateKeyFile)) {
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyInput, StandardCharsets.UTF_8));
            Object object = pemParser.readObject();
            BouncyCastleProvider provider = new BouncyCastleProvider();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(provider);
            if (object instanceof PEMEncryptedKeyPair) {
                PEMDecryptorProvider decryptionProvider = new JcePEMDecryptorProviderBuilder().setProvider(provider)
                    .build(keyPassword.toCharArray());
                PEMKeyPair keypair = ((PEMEncryptedKeyPair) object).decryptKeyPair(decryptionProvider);
                keyPair = converter.getKeyPair(keypair);
            } else {
                keyPair = converter.getKeyPair((PEMKeyPair) object);
            }
        }
        if (keyPair == null) {
            log.error("keyPair is null");
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", cert);
        keyStore.setKeyEntry("private-key", keyPair.getPrivate(), keyPassword.toCharArray(),
            new Certificate[] {cert});

        return keyStore;
    }
}
