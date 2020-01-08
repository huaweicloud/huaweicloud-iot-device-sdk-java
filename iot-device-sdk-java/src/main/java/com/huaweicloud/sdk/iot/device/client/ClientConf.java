package com.huaweicloud.sdk.iot.device.client;

import java.security.KeyStore;

/**
 * 客户端配置
 */
public class ClientConf {


    /**
     * 设备id，在平台注册设备获得，生成规则：productId_nodeId
     */
    String deviceId;


    /**
     * 设备秘钥
     */
    String secret;

    /**
     * 设备接入平台地址，比如tcp://localhost:1883 或者 ssl://localhost:8883
     */
    String serverUri;


    /**
     * 设备引导地址，配置了此参数就会先到引导地址引导，获取serverUri
     */
    String bootstrapUri;

    /**
     * 协议类型，MQTT或HTTP2，默认为MQTT
     */
    String protocol;

    /**
     * 离线消息缓存队列大小，默认5000，仅MQTT协议支持
     */
    Integer bufferSize;

    String deviceCert;

    String deviceCertKey;

    String keyPassword;

    KeyStore keyStore;

    /**
     * 客户端qos，0或1，默认1，仅MQTT协议支持
     */
    int qos = 1;


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getBootstrapUri() {
        return bootstrapUri;
    }

    public void setBootstrapUri(String bootstrapUri) {
        this.bootstrapUri = bootstrapUri;
    }

    public String getDeviceCert() {
        return deviceCert;
    }

    public void setDeviceCert(String deviceCert) {
        this.deviceCert = deviceCert;
    }

    public String getDeviceCertKey() {
        return deviceCertKey;
    }

    public void setDeviceCertKey(String deviceCertKey) {
        this.deviceCertKey = deviceCertKey;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

}
