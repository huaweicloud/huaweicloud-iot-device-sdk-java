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

package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultBootstrapActionListener implements ActionListener {

    private static final Logger log = LogManager.getLogger(DefaultBootstrapActionListener.class);

    /**
     * BootstrapRequestTrigger是平台系统字段，如果收到此字段，设备侧需要发起引导。
     */
    private static final String BOOTSTRAP_MESSAGE = "BootstrapRequestTrigger";

    private final BootstrapClient bootstrapClient;

    DefaultBootstrapActionListener(BootstrapClient bootstrapClient) {
        this.bootstrapClient = bootstrapClient;
    }

    @Override
    public void onSuccess(Object context) {
        // 引导成功，获取到iot平台的地址
        String address = (String) context;
        log.info("bootstrap success, the address is {}", address);

        // 引导成功后关闭客户端
        bootstrapClient.close();

        // 与iot平台建立连接
        IoTDevice device = bootstrapClient.getIoTDevice("ssl://" + address);
        if (device == null || device.init() != 0) {
            return;
        }

        // 接收下发消息
        device.getClient().setDeviceMessageListener(deviceMessage -> {
            // 收到重引导消息，发起引导
            if (BOOTSTRAP_MESSAGE.equals(deviceMessage.getContent())) {
                // 关闭客户端
                device.getClient().close();

                // 创建引导客户端，发起引导
                BootstrapClient reBootstrapClient;
                try {
                    reBootstrapClient = bootstrapClient.clone();
                } catch (CloneNotSupportedException exp) {
                    log.error("clone bootstrap client failed: {}", exp.getMessage());
                    return;
                }

                DefaultBootstrapActionListener defaultBootstrapActionListener
                    = new DefaultBootstrapActionListener(reBootstrapClient);
                reBootstrapClient.bootstrap(defaultBootstrapActionListener);
            }
        });

        // 上报消息
        device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
    }

    @Override
    public void onFailure(Object context, Throwable var2) {
        // 引导失败
        log.error("bootstrap failed: {}", var2.getMessage());
    }
}
