package com.huaweicloud.bridge.sdk.handler;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.listener.BridgeDeviceDisConnListener;
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
public class DeviceDisConnHandlerTest {

    @Mock
    private BridgeClient bridgeClientMock;

    private DeviceDisConnHandler deviceDisConnHandlerUnderTest;

    @Before
    public void setUp() {
        deviceDisConnHandlerUnderTest = new DeviceDisConnHandler(bridgeClientMock);
    }

    /**
     * 用例编号:test_bridge_disconnect_handler_should_return_success
     * 用例标题:网桥处理断开连接结果成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，提前设置好listener
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用listener的onDisConnect函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_bridge_disconnect_handler_should_return_success() {
        // Setup
        final RawMessage message = new RawMessage("$oc/bridges/{bridge_id}/devices/{device_id}/sys/disconnect",
            "payload", 0);
        BridgeDeviceDisConnListener bridgeDeviceDisConnListenerMock = PowerMockito.mock(
            BridgeDeviceDisConnListener.class);
        when(bridgeClientMock.getBridgeDeviceDisConnListener()).thenReturn(bridgeDeviceDisConnListenerMock);

        // Run the test
        deviceDisConnHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNotEquals(null, bridgeClientMock.getBridgeDeviceDisConnListener());
        verify(bridgeDeviceDisConnListenerMock).onDisConnect(anyString());
    }
}
