package com.huaweicloud.bridge.sdk.handler;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.listener.BridgeCommandListener;
import com.huaweicloud.bridge.sdk.request.BridgeCommand;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
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
public class BridgeCommandHandlerTest {

    @Mock
    private BridgeClient bridgeClientMock;

    private BridgeCommandHandler bridgeCommandHandlerUnderTest;

    @Before
    public void setUp() {
        bridgeCommandHandlerUnderTest = new BridgeCommandHandler(bridgeClientMock);
    }

    /**
     * 用例编号:test_bridge_command_handler_should_return_success
     * 用例标题:网桥处理命令成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，提前设置好listener
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用listener的onCommand函数
     * 修改记录:2022/08/27 l00426280
     */
    @Test
    public void test_bridge_command_handler_should_return_success() {
        // Set up
        Command command = new Command();
        command.setCommandName("commandName");
        command.setDeviceId("deviceId");
        command.setServiceId("serviceId");
        final RawMessage message = new RawMessage(
            "$oc/bridges/{bridge_id}/devices/{device_id}/sys/commands/request_id={request_id}", command.toString(),
            0);

        BridgeCommandListener bridgeCommandListenerMock = PowerMockito.mock(BridgeCommandListener.class);
        when(bridgeClientMock.getBridgeCommandListener()).thenReturn(bridgeCommandListenerMock);

        // Run the test
        bridgeCommandHandlerUnderTest.messageHandler(message);

        // Verify the results
        assertNotEquals(null, bridgeClientMock.getBridgeCommandListener());
        verify(bridgeCommandListenerMock).onCommand(anyString(), anyString(), any(BridgeCommand.class));
    }
}
