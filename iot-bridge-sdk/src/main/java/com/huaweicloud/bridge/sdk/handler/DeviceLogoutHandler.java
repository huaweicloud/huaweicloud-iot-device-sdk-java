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

package com.huaweicloud.bridge.sdk.handler;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.sdk.iot.device.client.handler.MessageReceivedHandler;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DeviceLogoutHandler implements MessageReceivedHandler {
    private static final Logger log = LogManager.getLogger(
        DeviceLogoutHandler.class);

    private BridgeClient bridgeClient;

    public DeviceLogoutHandler(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public void messageHandler(RawMessage message) {
        log.debug("received the response of the device under one bridge logouts, the  message is {}",
            message);
        String requestId = IotUtil.getRequestId(message.getTopic());
        String deviceId = IotUtil.getDeviceId(message.getTopic());
        Map map = JsonUtil.convertJsonStringToObject(message.toString(), Map.class);
        if (map == null) {
            log.warn("the response of device logout is invalid. ");
            return;
        }
        int resultCode = (int) map.get(BridgeSDKConstants.RESULET_CODE);

        if (bridgeClient.getLogoutListener() != null) {
            bridgeClient.getLogoutListener().onLogout(requestId, deviceId, map);
            return;
        }

        CompletableFuture<Integer> future = bridgeClient.getRequestIdCache().getFuture(requestId);
        Optional.ofNullable(future)
            .ifPresent(integerCompletableFuture -> integerCompletableFuture.complete(resultCode));
    }
}
