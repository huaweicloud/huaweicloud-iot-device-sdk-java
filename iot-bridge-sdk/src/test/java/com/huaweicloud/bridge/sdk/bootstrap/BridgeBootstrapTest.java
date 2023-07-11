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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.huaweicloud.bridge.sdk.BridgeDevice;
import com.huaweicloud.sdk.iot.device.client.ClientConf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BridgeClientConf.class, BridgeDevice.class})
public class BridgeBootstrapTest {

    @Mock
    private BridgeDevice bridgeDeviceMock;

    private BridgeBootstrap bridgeBootstrapUnderTest;

    @Before
    public void setUp() {
        bridgeBootstrapUnderTest = new BridgeBootstrap();
    }

    /**
     * 用例编号:test_init_bridge_success
     * 用例标题:网桥初始化
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥Id和密钥
     * 测试步骤:网桥初始化
     * 预期结果:网桥初始化接口被调用
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_init_bridge_success() {
        PowerMockito.mockStatic(BridgeDevice.class);
        PowerMockito.when(BridgeDevice.getInstance(any(ClientConf.class))).thenReturn(bridgeDeviceMock);
        bridgeBootstrapUnderTest.initBridge();

        verify(bridgeDeviceMock).init();
    }
}
