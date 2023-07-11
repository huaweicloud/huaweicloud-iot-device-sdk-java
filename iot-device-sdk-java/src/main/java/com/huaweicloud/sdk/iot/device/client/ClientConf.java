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

package com.huaweicloud.sdk.iot.device.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huaweicloud.sdk.iot.device.constants.Constants;

import java.io.File;
import java.security.KeyStore;

/**
 * 客户端配置
 */
public class ClientConf implements Cloneable {
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

    /**
     * scopeId,在设备发放的自注册场景下使用
     */
    private String scopeId;

    /**
     * 0代表直连方式，3代表网桥方式(跟平台保持一致)。默认是0
     */
    private int mode;

    /**
     * file, iot平台的ca证书，用于设备侧校验平台
     */
    private File file;

    /**
     * 是否采用国密加密方式
     */
    private boolean isGmssl = false;

    /**
     * 时间戳校验方式
     */
    private int checkStamp = Constants.CHECK_STAMP_SHA256_OFF;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
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

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    @Override
    public ClientConf clone() throws CloneNotSupportedException {
        return (ClientConf) super.clone();
    }

    /**
     * 设置是否启用国密
     *
     * @param flag 国密开关
     */
    public void setGmssl(boolean flag) {
        isGmssl = flag;
    }

    public boolean isGmssl() {
        return isGmssl;
    }

    /**
     * 设置时间戳校验方式
     *
     * @param checkMethod 时间戳校验方式
     */
    public void setCheckStamp(int checkMethod) {
        checkStamp = checkMethod;
    }

    public int getCheckStamp() {
        return checkStamp;
    }
}
