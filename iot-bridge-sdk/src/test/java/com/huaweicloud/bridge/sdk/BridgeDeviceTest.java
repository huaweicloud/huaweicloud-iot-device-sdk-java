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

import static org.junit.Assert.assertEquals;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.constants.Constants;

import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;

public class BridgeDeviceTest {
    private ClientConf clientConf = new ClientConf();

    private SecureRandom secureRandom = new SecureRandom();

    @Before
    public void setUp() {
        clientConf.setServerUri("ssl://100.93.541.101:8883");
        clientConf.setDeviceId("bridgeId");
        clientConf.setSecret("bridgeSecret");
        clientConf.setMode(Constants.CONNECT_OF_BRIDGE_MODE);
    }

    /**
     * 用例编号:test_getInstance_should_return_same_obj_when_input_diff_clientConf
     * 用例标题:两次通过clientConf获取BridgeDevice相同
     * 用例级别:Level 1
     * 预置条件:clientConf配置类
     * 测试步骤:通过clientConf调用getInstance函数两次
     * 预期结果:获取到的BridgeDevice是同一个对象
     * 修改记录:2022/06/22 
     */
    @Test
    public void test_getInstance_should_return_same_obj_when_input_diff_clientConf() {
        assertEquals(BridgeDevice.getInstance(clientConf), BridgeDevice.getInstance(clientConf));
    }

    /**
     * 用例编号:test_getInstance_should_throw_exception_when_input_device_mode
     * 用例标题:网桥通过非网桥模式创建异常
     * 用例级别:Level 1
     * 预置条件:clientConf配置类
     * 测试步骤:通过clientConf的普通设备模式调用getInstance一次
     * 预期结果:抛出IllegalArgumentException异常
     * 修改记录:2022/06/22 
     */
    @Test(expected = IllegalArgumentException.class)
    public void test_getInstance_should_throw_exception_when_input_device_mode() {
        int mode = secureRandom.nextInt(10);
        while (mode == Constants.CONNECT_OF_BRIDGE_MODE) {
            mode = secureRandom.nextInt(10);
        }
        clientConf.setMode(mode);
        BridgeDevice.getInstance(clientConf);
    }
}
