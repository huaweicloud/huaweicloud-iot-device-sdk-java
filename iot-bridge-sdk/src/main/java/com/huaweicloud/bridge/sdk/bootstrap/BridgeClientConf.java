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

package com.huaweicloud.bridge.sdk.bootstrap;

import java.io.File;

/**
 * 网桥客户端配置
 */
public class BridgeClientConf {

    /**
     * 平台接入地址变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_SERVER_IP = "NET_BRIDGE_SERVER_IP";


    /**
     * 平台接入端口变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_SERVER_PORT = "NET_BRIDGE_SERVER_PORT";

    /**
     * 网桥ID环境变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_ID = "NET_BRIDGE_ID";

    /**
     * 网桥密码环境变量名称
     *
     */
    private static final String ENV_NET_BRIDGE_SECRET = "NET_BRIDGE_SECRET";


    /**
     * 连接IoT平台的地址 样例：c20c0d18c2.st1.iotda-device.cn-north-4.myhuaweicloud.com
     *
     */
    private String serverIp;

    /**
     * 连接IoT平台的端口
     *
     */
    private String serverPort;

    /**
     * 连接IoT平台的网桥ID.
     *
     */
    private String bridgeId;

    /**
     * 连接IoT平台的网桥密码
     *
     */
    private String bridgeSecret;

    /**
     * 连接IoT平台的CA文件路径， 用于设备侧校验平台
     *
     */
    private File caFilePath;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getBridgeId() {
        return bridgeId;
    }

    public void setBridgeId(String bridgeId) {
        this.bridgeId = bridgeId;
    }

    public String getBridgeSecret() {
        return bridgeSecret;
    }

    public void setBridgeSecret(String bridgeSecret) {
        this.bridgeSecret = bridgeSecret;
    }

    public File getCaFilePath() {
        return caFilePath;
    }

    public void setCaFilePath(File caFilePath) {
        this.caFilePath = caFilePath;
    }

    public static BridgeClientConf fromEnv() {
        BridgeClientConf conf = new BridgeClientConf();
        conf.setServerIp(System.getenv(ENV_NET_BRIDGE_SERVER_IP));
        conf.setServerPort(System.getenv(ENV_NET_BRIDGE_SERVER_PORT));
        conf.setBridgeId(System.getenv(ENV_NET_BRIDGE_ID));
        conf.setBridgeSecret(System.getenv(ENV_NET_BRIDGE_SECRET));

        return conf;
    }
}
