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

import com.huaweicloud.bridge.sdk.BridgeDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 网桥启动初始化
 */
public class BridgeBootstrap {
    private static final Logger log = LogManager.getLogger(BridgeBootstrap.class);

    // 网桥模式
    private static final int CONNECT_OF_BRIDGE_MODE = 3;

    private BridgeDevice bridgeDevice;

    /**
     * 从环境变量获取网桥配置信息，初始化网桥。
     */
    public void initBridge() {
        BridgeClientConf conf = BridgeClientConf.fromEnv();
        initBridge(conf);
    }

    /**
     * 根据网桥配置信息，初始化网桥
     *
     * @param conf 网桥配置
     */
    public void initBridge(BridgeClientConf conf) {
        if (conf == null) {
            conf = BridgeClientConf.fromEnv();
        }
        bridgeOnline(conf);
    }

    private void bridgeOnline(BridgeClientConf conf) {
        ClientConf clientConf = new ClientConf();
        if (conf.getServerIp() != null && conf.getServerPort() != null) {
            clientConf.setServerUri("ssl://" + conf.getServerIp() + ":" + conf.getServerPort());
        }
        clientConf.setDeviceId(conf.getBridgeId());
        clientConf.setSecret(conf.getBridgeSecret());
        clientConf.setMode(CONNECT_OF_BRIDGE_MODE);
        clientConf.setFile(conf.getCaFilePath());

        BridgeDevice bridgeDev = BridgeDevice.getInstance(clientConf);
        if (bridgeDev.init() != 0) {
            log.error("Bridge can't login. please check!");
        }
        this.bridgeDevice = bridgeDev;
    }

    public BridgeDevice getBridgeDevice() {
        return bridgeDevice;
    }

}
