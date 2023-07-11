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

package com.huaweicloud.bridge.sdk;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.constants.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class BridgeDevice extends IoTDevice {
    private static final Logger log = LogManager.getLogger(BridgeDevice.class);

    private static BridgeDevice instance;

    private BridgeClient bridgeClient;

    private BridgeDevice(ClientConf clientConf) {
        super(clientConf);
        if (Constants.CONNECT_OF_BRIDGE_MODE != clientConf.getMode()) {
            throw new IllegalArgumentException("the bridge mode is invalid which the value should be 3.");
        }
        this.bridgeClient = new BridgeClient(clientConf, this);
    }

    // 此处采用单例模式，默认一个网桥服务，只会启动一个网桥，且网桥参数一致
    public static BridgeDevice getInstance(ClientConf clientConf) {
        if (Objects.isNull(instance)) {
            instance = new BridgeDevice(clientConf);
        }
        return instance;
    }

    @Override
    public int init() {
        log.debug("the bridge client starts to init. ");
        return bridgeClient.connect();
    }

    /**
     * 获取网桥设备客户端。获取到网桥设备客户端后，可以直接调用客户端提供的消息、属性、命令等接口
     *
     * @return 设备客户端实例
     */
    public BridgeClient getClient() {
        return this.bridgeClient;
    }

}
