package com.huaweicloud.sdk.iot.device;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class DemoUtil {

    private static final String TLS_VER = "TLSv1.2";
    private static Logger log = Logger.getLogger(DemoUtil.class);

    private static TrustManager[] getTrustManager() throws Exception {

        File serverCert = new File(DemoUtil.class.getClassLoader().getResource("ca.jks").getPath());

        try (InputStream stream = new FileInputStream(serverCert)) {
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(stream, null);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            TrustManager[] tm = tmf.getTrustManagers();
            return tm;
        }
    }

    private static KeyManager[] getKeyManager(File deviceCert, File deviceCertKey, String password) throws Exception {

        if (deviceCert == null || deviceCertKey == null || password == null) {
            log.error("input null");
            return null;
        }

        Certificate cert = null;
        try (FileInputStream inputStream = new FileInputStream(deviceCert)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(inputStream);
        }

        KeyPair keyPair = null;
        try (FileInputStream keyInput = new FileInputStream(deviceCertKey)) {
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyInput, StandardCharsets.UTF_8));
            Object object = pemParser.readObject();
            BouncyCastleProvider provider = new BouncyCastleProvider();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(provider);
            if (object instanceof PEMEncryptedKeyPair) {
                PEMDecryptorProvider decryptionProvider = new JcePEMDecryptorProviderBuilder().setProvider(provider).build(password.toCharArray());
                PEMKeyPair keypair = ((PEMEncryptedKeyPair) object).decryptKeyPair(decryptionProvider);
                keyPair = converter.getKeyPair(keypair);
            } else {
                keyPair = converter.getKeyPair((PEMKeyPair) object);
            }
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", cert);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        if (null != password && null != keyPair) {
            keyStore.setKeyEntry("private-key", keyPair.getPrivate(), password.toCharArray(),
                    new Certificate[]{cert});
            kmf.init(keyStore, password.toCharArray());
        }

        return kmf.getKeyManagers();
    }

    private static SSLContext getSSLContextWithKeys(String clientCert, String clientKey, String keyPassword) {

        File clientCertFile = new File(clientCert);
        File clientKeyFile = new File(clientKey);

        SSLContext context = null;
        try {
            context = SSLContext.getInstance(TLS_VER);
            context.init(getKeyManager(clientCertFile, clientKeyFile, keyPassword),
                    getTrustManager(), new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return context;
    }

    private static SSLContext getSSLContextWithKeystore(KeyStore keyStore, String keyPassword) throws Exception {
        SSLContext context = SSLContext.getInstance(TLS_VER);

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, keyPassword.toCharArray());
        context.init(managerFactory.getKeyManagers(), getTrustManager(), null);
        return context;
    }

    public static KeyStore getKeyStore(String deviceCert, String deviceCertKey, String keyPassword) throws Exception {
        if (deviceCert == null || deviceCertKey == null || keyPassword == null) {
            log.error("input null");
            return null;
        }

        Certificate cert = null;
        try (FileInputStream inputStream = new FileInputStream(deviceCert)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(inputStream);
        }

        KeyPair keyPair = null;
        try (FileInputStream keyInput = new FileInputStream(deviceCertKey)) {
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyInput, StandardCharsets.UTF_8));
            Object object = pemParser.readObject();
            BouncyCastleProvider provider = new BouncyCastleProvider();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(provider);
            if (object instanceof PEMEncryptedKeyPair) {
                PEMDecryptorProvider decryptionProvider = new JcePEMDecryptorProviderBuilder().setProvider(provider).build(keyPassword.toCharArray());
                PEMKeyPair keypair = ((PEMEncryptedKeyPair) object).decryptKeyPair(decryptionProvider);
                keyPair = converter.getKeyPair(keypair);
            } else {
                keyPair = converter.getKeyPair((PEMKeyPair) object);
            }
        }
        if (keyPair == null){
            log.error("keyPair is null");
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("certificate", cert);
        keyStore.setKeyEntry("private-key", keyPair.getPrivate(), keyPassword.toCharArray(),
                new Certificate[]{cert});

        return keyStore;
    }

    public static SSLContext getSSLContext(ClientConf clientConf) throws Exception {

        if (clientConf.getKeyStore() != null) {

            return getSSLContextWithKeystore(clientConf.getKeyStore(), clientConf.getKeyPassword());
        } else if (clientConf.getDeviceCert() != null) {
            return getSSLContextWithKeys(clientConf.getDeviceCert(), clientConf.getDeviceCertKey(),
                    clientConf.getKeyPassword());
        } else {
            SSLContext sslContext = SSLContext.getInstance(TLS_VER);
            sslContext.init(null, getTrustManager(), new SecureRandom());
        }

        return null;
    }
}
