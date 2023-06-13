package com.huaweicloud.bridge.sdk.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import com.huaweicloud.bridge.sdk.BridgeClient;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.bridge.sdk.listener.LoginListener;
import com.huaweicloud.bridge.sdk.request.RequestIdCache;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.script.*"})
public class DeviceLoginHandlerTest {

    @Mock
    private BridgeClient bridgeClientMock;

    private DeviceLoginHandler deviceLoginHandlerUnderTest;

    @Before
    public void setUp() {
        deviceLoginHandlerUnderTest = new DeviceLoginHandler(bridgeClientMock);
    }

    /**
     * 用例编号:test_login_handler_should_return_success_when_listener_is_not_null
     * 用例标题:网桥处理异步登录结果成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，提前设置好listener
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用listener的onLogin函数
     * 修改记录:2022/08/27 l00426280
     */
    @Test
    public void test_login_handler_should_return_success_when_listener_is_not_null() {
        // Setup
        Map<String, Object> map = new HashMap<>();
        map.put(BridgeSDKConstants.RESULET_CODE, 0);

        final RawMessage message = new RawMessage(
            "$oc/bridges/{bridge_id}/devices/{device_id}/sys/login/response/request_id={request_id}",
            JsonUtil.convertObject2String(map), 0);
        LoginListener loginListenerMock = PowerMockito.mock(LoginListener.class);
        PowerMockito.when(bridgeClientMock.getLoginListener()).thenReturn(loginListenerMock);

        // Run the test
        deviceLoginHandlerUnderTest.messageHandler(message);

        // Verify the results
        verify(loginListenerMock).onLogin(anyString(), anyString(), anyInt());
    }

    /**
     * 用例编号:test_login_handler_should_return_success_when_listener_is_null
     * 用例标题:网桥处理同步登录结果成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台
     * 测试步骤:调用messageHandler接口
     * 预期结果:成功调用complete函数
     * 修改记录:2022/08/27 l00426280
     */
    @Test
    public void test_login_handler_should_return_success_when_listener_is_null() {
        // Setup
        Map<String, Object> map = new HashMap<>();
        map.put(BridgeSDKConstants.RESULET_CODE, 0);

        final RawMessage message = new RawMessage(
            "$oc/bridges/{bridge_id}/devices/{device_id}/sys/login/response/request_id={request_id}",
            JsonUtil.convertObject2String(map), 0);
        PowerMockito.when(bridgeClientMock.getLoginListener()).thenReturn(null);
        CompletableFuture completableFutureMock = PowerMockito.mock(CompletableFuture.class);
        RequestIdCache requestIdCacheMock = PowerMockito.mock(RequestIdCache.class);

        PowerMockito.when(bridgeClientMock.getRequestIdCache()).thenReturn(requestIdCacheMock);
        PowerMockito.when(requestIdCacheMock.getFuture(anyString())).thenReturn(completableFutureMock);
        PowerMockito.when(completableFutureMock.complete(any())).thenReturn(true);

        // Run the test
        deviceLoginHandlerUnderTest.messageHandler(message);

        // Verify the results
        verify(completableFutureMock).complete(any());
    }

}
