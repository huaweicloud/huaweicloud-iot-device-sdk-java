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
     * 修改记录:2022/08/27 l00426280
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
