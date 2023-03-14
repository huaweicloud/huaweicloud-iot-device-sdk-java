package com.huaweicloud.sdk.iot.device.demo.utils;

import java.security.KeyStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
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
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class CertificateUtil {
    private static final Logger log = LogManager.getLogger(CertificateUtil.class);

    /**
     * 读取PEM格式的证书
     * 
     * @param certificateFile 证书文件路径
     * @param privateKeyFile  证书私钥文件路径
     * @param keyPassword     证书私钥密码，如私钥未加密，则传入null
     * @return 证书对象
     */
    public static KeyStore getKeyStore(String certificateFile, String privateKeyFile, String keyPassword)
            throws Exception {
        if (certificateFile == null || privateKeyFile == null) {
            log.error("certificateFile or privateKeyFile must not be null");
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
            } else if (object instanceof PrivateKeyInfo) {
                keyPair = new KeyPair(null, converter.getPrivateKey((PrivateKeyInfo) object));
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
                new Certificate[] { cert });

        return keyStore;
    }

    /**
     * 读取keystore格式证书
     * 
     * @return KeyStore证书对象
     */
    public static KeyStore getKeyStore(String certificateFile, String keyPassword) throws Exception {
        if (certificateFile == null) {
            log.error("certificateFile or privateKeyFile must not be null");
            return null;
        }

        if (keyPassword == null) {
            keyPassword = "";
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(certificateFile), keyPassword.toCharArray());
        return keyStore;
    }
}