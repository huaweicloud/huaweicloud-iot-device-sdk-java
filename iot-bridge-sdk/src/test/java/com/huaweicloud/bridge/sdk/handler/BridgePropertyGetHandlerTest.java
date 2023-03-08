package com.huaweicloud.bridge.sdk.handler;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.listener.BridgePropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.script.*"})
public class BridgePropertyGetHandlerTest {

    @Mock
    private BridgeClient bridgeClientMock;

    private BridgePropertyGetHandler bridgePropertyGetHandlerUnderTest;

    @Before
    public void setUp() {
        bridgePropertyGetHandlerUnderTest = new BridgePropertyGetHandler(bridgeClientMock);
    }

    /**
     * 用例编号:test_bridge_prop_get_handler_should_return_success
     * 用例标题:网桥处理平台属性查询成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，提前设置好listener
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用listener的onPropertiesGet函数
     * 修改记录:2022/08/27 l00426280
     */
    @Test
    public void test_bridge_prop_get_handler_should_return_success() {
        // Set up
        PropsGet propsGet = new PropsGet();
        propsGet.setDeviceId("deviceId");
        propsGet.setServiceId("serviceId");
        final RawMessage message = new RawMessage(
            "$oc/bridges/{bridge_id}/devices/{device_id}/sys/properties/get/request_id={request_id}",
            propsGet.toString(), 0);
        BridgePropertyListener bridgePropertyListenerMock = PowerMockito.mock(
            BridgePropertyListener.class);

        when(bridgeClientMock.getBridgePropertyListener()).thenReturn(bridgePropertyListenerMock);

        // Run the test
        bridgePropertyGetHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNotEquals(null, bridgeClientMock.getBridgePropertyListener());
        verify(bridgePropertyListenerMock).onPropertiesGet(anyString(), anyString(), anyString());
    }
}
