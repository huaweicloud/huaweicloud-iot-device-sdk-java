package com.huaweicloud.sdk.iot.device.demo;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class DefaultX509TrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
