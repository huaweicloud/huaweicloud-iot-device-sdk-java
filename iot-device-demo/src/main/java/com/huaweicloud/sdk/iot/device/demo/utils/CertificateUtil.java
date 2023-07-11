/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.demo.utils;

import java.security.KeyStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
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
        try (FileInputStream keyInput = new FileInputStream(privateKeyFile);
            InputStreamReader inputStreamReader = new InputStreamReader(keyInput, StandardCharsets.UTF_8);
            PEMParser pemParser = new PEMParser(inputStreamReader);
        ) {
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
            new Certificate[] {cert});

        return keyStore;
    }

    /**
     * 读取国密PEM格式的证书
     *
     * @param ks 出参keyStore
     * @param gmCert  国密证书相关信息
     * @return 证书读取成功与否
     */
    public static boolean getGmKeyStore(KeyStore ks, GmCertificate gmCert)
            throws Exception {
        if (ks == null || gmCert == null || !gmCert.checkValid()) {
            log.error("keystore or gmCert must not be null");
            return false;
        }

        Certificate cert = null;
        try (FileInputStream inputStream = new FileInputStream(gmCert.getCertFile())) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(inputStream);
        }

        KeyPair keyPair = null;
        try (FileInputStream keyInput = new FileInputStream(gmCert.getKeyFile());
             InputStreamReader inputStreamReader = new InputStreamReader(keyInput, StandardCharsets.UTF_8);
             PEMParser pemParser = new PEMParser(inputStreamReader);
        ) {
            Object object = pemParser.readObject();
            BouncyCastleProvider provider = new BouncyCastleProvider();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(provider);
            if (object instanceof PEMEncryptedKeyPair) {
                PEMDecryptorProvider decryptionProvider = new JcePEMDecryptorProviderBuilder().setProvider(provider)
                        .build(gmCert.getPassword().toCharArray());
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
            return false;
        }

        ks.setCertificateEntry(gmCert.getCertAlias(), cert);
        ks.setKeyEntry(gmCert.getKeyAlias(), keyPair.getPrivate(), gmCert.getPassword().toCharArray(), new Certificate[] {cert});

        return true;
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
        try (FileInputStream fileInputStream = new FileInputStream(certificateFile);) {
            keyStore.load(fileInputStream, keyPassword.toCharArray());
        } catch (Exception e) {
            log.error("load keyStore failed, the reason is {}", e.getMessage());
        }
        return keyStore;
    }
}