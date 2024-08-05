/*
 * Copyright (c) 2020-2024 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleBootstrapActionListener implements ActionListener {
    private static final Logger log = LogManager.getLogger(SimpleBootstrapActionListener.class);

    private final BootstrapClient bootstrapClient;

    public SimpleBootstrapActionListener(BootstrapClient bootstrapClient) {
        this.bootstrapClient = bootstrapClient;
    }

    @Override
    public void onSuccess(Object context) {
        // 引导成功，获取到iot平台的地址
        ObjectNode node = JsonUtil.convertJsonStringToObject((String) context, ObjectNode.class);
        String address = node.get("address").asText();
        log.info("bootstrap success, the address is {}", address);
        final JsonNode deviceSecretNode = node.get("deviceSecret");
        String deviceSecret = null;
        if (deviceSecretNode != null) {
            deviceSecret = deviceSecretNode.asText();
        }

        // 引导成功后关闭客户端
        bootstrapClient.close();

        // 与iot平台建立连接，上报消息
        IoTDevice device = bootstrapClient.getIoTDevice("ssl://" + address);
        if (deviceSecret != null) {
            device.getClient().getClientConf().setSecret(deviceSecret);
        }
        if (device == null || device.init() != 0) {
            return;
        }
        device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
    }

    @Override
    public void onFailure(Object context, Throwable throwable) {
        // 引导失败
        log.error("bootstrap failed: {}", throwable.getMessage());
    }
}
