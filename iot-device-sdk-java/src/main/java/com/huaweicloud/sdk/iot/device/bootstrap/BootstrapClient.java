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

package com.huaweicloud.sdk.iot.device.bootstrap;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.transport.mqtt.MqttConnection;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 引导客户端，用于设备引导来获取服务端地址
 */
public class BootstrapClient implements RawMessageListener, Cloneable {
    private static final Logger log = LogManager.getLogger(BootstrapClient.class);

    /**
     * 设备发放的设备侧CA证书，注意与IoTDA的设备侧区分开
     *
     * @deprecated 避免在sdk中固定路径，此处常量后续将从sdk中移除
     */
    @Deprecated
    private static final String BOOTSTRAP_CA_RES_PATH = "ca.jks";

    /**
     * 设备发放定义的TOPIC
     */
    private static final String BOOTSTRAP_PUBLISH_TOPIC = "$oc/devices/%s/sys/bootstrap/up";

    private static final String BOOTSTRAP_SUBSCRIBE_TOPIC = "$oc/devices/%s/sys/bootstrap/down";

    /**
     * 客户端配置
     */
    private final ClientConf clientConf;

    /**
     * 平台CA证书提供者，用于设备端验证服务端
     */
    private final PlatformCaProvider platformCaProvider;

    private final String deviceId;

    private final Connection connection;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ActionListener listener;

    /**
     * 构造函数，使用密码创建
     *
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     * @deprecated 为避免在sdk中固定路径，此方法后续将移除，推荐使用 {@link #BootstrapClient(String bootstrapUri, String deviceId, String deviceSecret, PlatformCaProvider platformCaProvider)} 替代本方法
     */
    @Deprecated
    public BootstrapClient(String bootstrapUri, String deviceId, String deviceSecret) {
        this(bootstrapUri, deviceId, deviceSecret, getPlatformCaProvider());
    }

    /**
     * 构造函数，使用密码创建
     *
     * @param bootstrapUri       bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId           设备id
     * @param deviceSecret       设备密码
     * @param platformCaProvider 平台CA证书提供者
     * @since 1.1.3
     */
    public BootstrapClient(String bootstrapUri, String deviceId, String deviceSecret, PlatformCaProvider platformCaProvider) {

        ClientConf conf = new ClientConf();
        conf.setServerUri(bootstrapUri);
        conf.setFile(platformCaProvider.getBootstrapCaFile());
        conf.setDeviceId(deviceId);
        conf.setSecret(deviceSecret);
        this.clientConf = conf;
        this.deviceId = deviceId;
        this.platformCaProvider = platformCaProvider;
        this.connection = new MqttConnection(conf, this);
        log.info("create BootstrapClient, the deviceId is {}", conf.getDeviceId());

    }

    /**
     * 构造函数，使用证书创建
     *
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param keyStore     证书容器
     * @param keyPassword  证书密码
     * @deprecated 为避免在sdk中固定路径，此方法后续将移除，推荐使用 {@link #BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword, PlatformCaProvider platformCaProvider)} 替代本方法
     */
    @Deprecated
    public BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword) {
        this(bootstrapUri, deviceId, keyStore, keyPassword, getPlatformCaProvider());
    }

    /**
     * 构造函数，使用证书创建
     *
     * @param bootstrapUri       bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId           设备id
     * @param keyStore           证书容器
     * @param keyPassword        证书密码
     * @param platformCaProvider 平台CA证书提供者
     * @since 1.1.3
     */
    public BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword, PlatformCaProvider platformCaProvider) {

        ClientConf conf = new ClientConf();
        conf.setServerUri(bootstrapUri);
        conf.setFile(platformCaProvider.getBootstrapCaFile());
        conf.setDeviceId(deviceId);
        conf.setKeyPassword(keyPassword);
        conf.setKeyStore(keyStore);
        this.clientConf = conf;
        this.deviceId = deviceId;
        this.platformCaProvider = platformCaProvider;
        this.connection = new MqttConnection(conf, this);
        log.info("create BootstrapClient, the deviceId is {}", conf.getDeviceId());
    }

    /**
     * 构造函数，自注册场景下证书创建
     *
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param keyStore     证书容器
     * @param keyPassword  证书密码
     * @param scopeId      scopeId, 自注册场景可从物联网平台获取
     * @deprecated 为避免在sdk中固定路径，此方法后续将移除，推荐使用 {@link #BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword,
     * String scopeId, PlatformCaProvider platformCaProvider)} 替代本方法
     */
    @Deprecated
    public BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword, String scopeId) {
        this(bootstrapUri, deviceId, keyStore, keyPassword, scopeId, getPlatformCaProvider());
    }

    /**
     * 构造函数，自注册场景下证书创建
     *
     * @param bootstrapUri       bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId           设备id
     * @param keyStore           证书容器
     * @param keyPassword        证书密码
     * @param scopeId            scopeId, 自注册场景可从物联网平台获取
     * @param platformCaProvider 平台CA证书提供者
     * @since 1.1.3
     */
    public BootstrapClient(String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword,
        String scopeId, PlatformCaProvider platformCaProvider) {
        ClientConf conf = new ClientConf();
        conf.setServerUri(bootstrapUri);
        conf.setFile(platformCaProvider.getBootstrapCaFile());
        conf.setDeviceId(deviceId);
        conf.setKeyStore(keyStore);
        conf.setKeyPassword(keyPassword);
        conf.setScopeId(scopeId);
        this.clientConf = conf;
        this.deviceId = deviceId;
        this.platformCaProvider = platformCaProvider;
        this.connection = new MqttConnection(conf, this);
        log.info("create BootstrapClient, the deviceId is {}", conf.getDeviceId());
    }

    @Override
    public void onMessageReceived(RawMessage message) {
        String bsTopic = String.format(BOOTSTRAP_SUBSCRIBE_TOPIC, this.deviceId);
        if (message.getTopic().equals(bsTopic)) {
            ObjectNode node = JsonUtil.convertJsonStringToObject(message.toString(), ObjectNode.class);
            String address = node.get("address").asText();
            log.info("bootstrap ok, the address is {}", address);

            Future<String> success = executorService.submit(() -> listener.onSuccess(address), "success");
            String result = "";
            try {
                result = success.get();
            } catch (Exception e) {
                log.error("get submit result failed, {}", e.getMessage());
            }

            if (result.equals("success")) {
                log.debug("submit task succeeded");
            }

        }
    }

    private static DefaultPlatformCaProvider getPlatformCaProvider() {
        return new DefaultPlatformCaProvider(BOOTSTRAP_CA_RES_PATH);
    }

    /**
     * 发起设备引导
     *
     * @param listener 监听器用来接收引导结果
     * @throws IllegalArgumentException 参数非法异常
     */
    public void bootstrap(ActionListener listener) throws IllegalArgumentException {

        this.listener = listener;

        if (connection.connect() != 0) {
            log.error("connect failed");
            listener.onFailure(null, new Exception("connect failed"));
            return;
        }

        String bsTopic = String.format(BOOTSTRAP_SUBSCRIBE_TOPIC, this.deviceId);

        connection.subscribeTopic(bsTopic, null, 0);

        String topic = String.format(BOOTSTRAP_PUBLISH_TOPIC, this.deviceId);
        RawMessage rawMessage = new RawMessage(topic, "");

        connection.publishMessage(rawMessage, null);

    }

    /**
     * 获取IoTDevice
     *
     * @param serverUri 服务端地址
     * @return 设备类
     * @since 1.1.3
     */
    public IoTDevice getIoTDevice(String serverUri) {
        try {
            ClientConf conf = this.clientConf.clone();
            conf.setServerUri(serverUri);
            conf.setFile(this.platformCaProvider.getIotCaFile());
            return new IoTDevice(conf);
        } catch (CloneNotSupportedException exp) {
            log.error("get iot device failed: {}", exp.getMessage());
            return null;
        }
    }

    /**
     * 关闭客户端
     */
    public void close() {
        connection.close();
        executorService.shutdown();
    }

    @Override
    public BootstrapClient clone() throws CloneNotSupportedException {
        return (BootstrapClient) super.clone();
    }
}
