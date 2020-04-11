package com.huaweicloud.sdk.iot.device.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.KeyStore;

/**
 * 客户端配置
 */

public class ClientConf {


    /**
     * 设备id，在平台注册设备获得，生成规则：productId_nodeId
     */
    private String deviceId;


    /**
     * 设备密码，使用密码认证时填写
     */
    private String secret;

    /**
     * 设备接入平台地址，比如tcp://localhost:1883 或者 ssl://localhost:8883
     */
    private String serverUri;

    /**
     * 协议类型，当前仅支持mqtt
     */
    private String protocol;

    /**
     * 离线消息缓存队列大小，默认5000，仅MQTT协议支持
     */
    private Integer offlineBufferSize;

    /**
     * keystore格式的证书，使用证书认证时传入keyStore和keyPassword
     */
    @JsonIgnore
    private KeyStore keyStore;

    /**
     * 私钥密码
     */
    private String keyPassword;

    /**
     * 客户端qos，0或1，默认1，仅MQTT协议支持
     */
    private int qos = 1;


    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备id
     *
     * @param deviceId 设备id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSecret() {
        return secret;
    }

    /**
     * 设置设备密码
     *
     * @param secret 设备密码
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServerUri() {
        return serverUri;
    }

    /**
     * 设置服务端地址
     *
     * @param serverUri 服务端地址
     */
    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * 设置设备接入协议
     *
     * @param protocol 设备接入协议
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getOfflineBufferSize() {
        return offlineBufferSize;
    }

    /**
     * 设置离线缓存大小
     *
     * @param offlineBufferSize 离线缓存大小
     */
    public void setOfflineBufferSize(Integer offlineBufferSize) {
        this.offlineBufferSize = offlineBufferSize;
    }

    public int getQos() {
        return qos;
    }

    /**
     * 客户端qos
     *
     * @param qos 客户端qos
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * 设置私钥密码
     *
     * @param keyPassword 私钥密码
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * 设置证书仓库
     *
     * @param keyStore 证书仓库
     */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

}
