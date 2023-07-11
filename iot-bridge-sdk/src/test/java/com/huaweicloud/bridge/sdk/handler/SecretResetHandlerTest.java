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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.listener.ResetDeviceSecretListener;
import com.huaweicloud.bridge.sdk.response.ResetDeviceSecretResponse;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.script.*"})
public class SecretResetHandlerTest {

    @Mock
    private BridgeClient bridgeClientMock;

    private SecretResetHandler secretResetHandlerUnderTest;

    @Before
    public void setUp() {
        secretResetHandlerUnderTest = new SecretResetHandler(bridgeClientMock);
    }

    /**
     * 用例编号:test_reset_secret_handler_should_return_success
     * 用例标题:网桥处理重置密钥结果
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，提前设置好listener
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用listener的onResetDeviceSecret函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_reset_secret_handler_should_return_success() {
        // Setup
        ResetDeviceSecretResponse resetDeviceSecretResponse = new ResetDeviceSecretResponse();
        resetDeviceSecretResponse.setResultCode(0);
        Map<String, Object> map = new HashMap<>();
        map.put("new_secret", "87654321");
        resetDeviceSecretResponse.setParas(map);
        final RawMessage message = new RawMessage(
            "$oc/bridges/{bridge_id}/devices/{device_id}/sys/reset_secret/response/request_id={request_id}",
            resetDeviceSecretResponse.toString(),
            0);
        ResetDeviceSecretListener resetDeviceSecretListenerMock = PowerMockito.mock(ResetDeviceSecretListener.class);
        when(bridgeClientMock.getResetDeviceSecretListener()).thenReturn(resetDeviceSecretListenerMock);

        // Run the test
        secretResetHandlerUnderTest.messageHandler(message);

        // Verify the results
        verify(resetDeviceSecretListenerMock).onResetDeviceSecret(anyString(), anyString(), anyInt(), anyString());
    }
}
