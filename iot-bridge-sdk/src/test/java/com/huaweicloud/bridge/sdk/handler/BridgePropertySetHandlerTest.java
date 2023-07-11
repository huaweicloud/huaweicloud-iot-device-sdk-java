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

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.listener.BridgePropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.script.*"})
public class BridgePropertySetHandlerTest {

    @Mock
    private BridgeClient bridgeClientMock;

    private BridgePropertySetHandler bridgePropertySetHandlerUnderTest;

    @Before
    public void setUp() {
        bridgePropertySetHandlerUnderTest = new BridgePropertySetHandler(bridgeClientMock);
    }

    /**
     * 用例编号:test_bridge_prop_set_handler_should_return_success
     * 用例标题:网桥处理平台属性设置成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，提前设置好listener
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用listener的onPropertiesSet函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_bridge_prop_set_handler_should_return_success() {
        // Set up
        PropsSet propsSet = new PropsSet();
        propsSet.setDeviceId("deviceId");
        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId("serviceId");
        Map<String, Object> map = new HashMap<>();
        map.put("property", "value");
        serviceProperty.setProperties(map);
        propsSet.setServices(Collections.singletonList(serviceProperty));
        final RawMessage message = new RawMessage(
            "$oc/bridges/{bridge_id}/devices/{device_id}/sys/properties/set/request_id={request_id}",
            propsSet.toString(), 0);
        BridgePropertyListener bridgePropertyListenerMock = PowerMockito.mock(BridgePropertyListener.class);

        when(bridgeClientMock.getBridgePropertyListener()).thenReturn(bridgePropertyListenerMock);

        // Run the test
        bridgePropertySetHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNotEquals(null, bridgeClientMock.getBridgePropertyListener());
        verify(bridgePropertyListenerMock).onPropertiesSet(anyString(), anyString(), any());
    }
}
