package com.huaweicloud.bridge.sdk;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.huaweicloud.bridge.sdk.request.DeviceSecret;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.constants.Constants;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BridgeClient.class, CompletableFuture.class})
@PowerMockIgnore({"javax.management.*", "javax.crypto.*", "javax.script.*"})
public class BridgeClientTest {
    private static final String deviceId = "deviceId";

    private static final String password = "password";

    private static final String requestId = "requestId";

    @Mock
    private Connection connMock;

    @Mock
    private ActionListener actionListenerMock;

    private BridgeClient bridgeClientUnderTest;

    @Before
    public void setUp() {
        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri("ssl://127.0.0.1:8883");
        clientConf.setDeviceId("bridge001");
        clientConf.setSecret("bridge001");
        clientConf.setMode(Constants.CONNECT_OF_BRIDGE_MODE);
        bridgeClientUnderTest = new BridgeClientForTest(connMock, clientConf, null);
    }

    /**
     * 用例编号:test_login_async_should_return_success
     * 用例标题:网桥设备发布异步login成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:提前设置监听器，然后网桥设备登录
     * 预期结果:回调函数被成功调用，并且回调了监听器中的onSuccess函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_login_async_should_return_success() {
        PowerMockito.doAnswer((Answer<Void>) invocationOnMock -> {
            ActionListener argument = invocationOnMock.getArgument(1);
            argument.onSuccess(any());
            return null;
        }).when(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));
        bridgeClientUnderTest.loginAsync(deviceId, password, requestId, actionListenerMock);
        verify(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));
        verify(actionListenerMock).onSuccess(any());
    }

    /**
     * 用例编号:test_login_async_should_return_failure
     * 用例标题:网桥设备发布异步login失败
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥，mock异常链路发布失败场景
     * 测试步骤:提前设置监听器，然后网桥设备登录
     * 预期结果:回调函数被成功调用，并且回调了监听器中的onFailure函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_login_async_should_return_failure() {
        PowerMockito.doAnswer((Answer<Void>) invocationOnMock -> {
            ActionListener argument = invocationOnMock.getArgument(1);
            argument.onFailure(any(), any());
            return null;
        }).when(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));

        bridgeClientUnderTest.loginAsync(deviceId, password, requestId, actionListenerMock);
        verify(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));
        verify(actionListenerMock).onFailure(any(), any());
    }

    /**
     * 用例编号:test_login_sync_should_return_success
     * 用例标题:网桥设备发布同步login成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥设备登录
     * 预期结果:正确返回结果0
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_login_sync_should_return_success() {
        CompletableFuture completableFutureMock = PowerMockito.mock(CompletableFuture.class);
        try {
            PowerMockito.whenNew(CompletableFuture.class).withNoArguments().thenReturn(completableFutureMock);
            PowerMockito.when(completableFutureMock.get(10, TimeUnit.MILLISECONDS)).thenReturn(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int result = bridgeClientUnderTest.loginSync(deviceId, password, 10);
        assertEquals(0, result);
    }

    /**
     * 用例编号:test_login_sync_should_return_failure
     * 用例标题:网桥设备发布同步login失败
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥，mock异常登录失败场景
     * 测试步骤:网桥设备登录
     * 预期结果:返回结果-1
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_login_sync_should_return_failure() {
        CompletableFuture completableFutureMock = PowerMockito.mock(CompletableFuture.class);
        try {
            PowerMockito.whenNew(CompletableFuture.class).withNoArguments().thenReturn(completableFutureMock);
            PowerMockito.when(completableFutureMock.get(100, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int result = bridgeClientUnderTest.loginSync(deviceId, password, 100);
        assertEquals(-1, result);
    }

    /**
     * 用例编号:test_logout_async_should_return_success
     * 用例标题:网桥设备发布异步logout成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:提前设置监听器，然后网桥设备登出
     * 预期结果:回调函数被成功调用，并且回调了监听器中的onSuccess函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_logout_async_should_return_success() {
        PowerMockito.doAnswer((Answer<Void>) invocationOnMock -> {
            ActionListener argument = invocationOnMock.getArgument(1);
            argument.onSuccess(any());
            return null;
        }).when(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));

        bridgeClientUnderTest.logoutAsync(deviceId, requestId, actionListenerMock);
        verify(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));
        verify(actionListenerMock).onSuccess(any());
    }

    /**
     * 用例编号:test_logout_async_should_returen_failure
     * 用例标题:网桥设备发布异步logout失败
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥，mock异常链路发布失败场景
     * 测试步骤:提前设置监听器，然后网桥设备登出
     * 预期结果:回调函数被成功调用，并且回调了监听器中的onFailure函数
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_logout_async_should_returen_failure() {
        PowerMockito.doAnswer((Answer<Void>) invocationOnMock -> {
            ActionListener argument = invocationOnMock.getArgument(1);
            argument.onFailure(any(), any());
            return null;
        }).when(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));

        bridgeClientUnderTest.logoutAsync(deviceId, requestId, actionListenerMock);
        verify(connMock).publishMessage(any(RawMessage.class), eq(actionListenerMock));
        verify(actionListenerMock).onFailure(any(), any());
    }

    /**
     * 用例编号:test_logout_sync_should_return_success
     * 用例标题:网桥设备发布同步logout成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥设备登出
     * 预期结果:正确返回结果0
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_logout_sync_should_return_success() {
        CompletableFuture completableFutureMock = PowerMockito.mock(CompletableFuture.class);
        try {
            PowerMockito.whenNew(CompletableFuture.class).withNoArguments().thenReturn(completableFutureMock);
            PowerMockito.when(completableFutureMock.get(10, TimeUnit.MILLISECONDS)).thenReturn(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int result = bridgeClientUnderTest.loginSync(deviceId, password, 10);
        assertEquals(0, result);
    }

    /**
     * 用例编号:test_logout_sync_should_return_failure
     * 用例标题:网桥设备发布同步logout失败
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥，mock异常链路发布失败场景
     * 测试步骤:网桥设备登出
     * 预期结果:正确返回结果-1
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_logout_sync_should_return_failure() {
        CompletableFuture completableFutureMock = PowerMockito.mock(CompletableFuture.class);
        try {
            PowerMockito.whenNew(CompletableFuture.class).withNoArguments().thenReturn(completableFutureMock);
            PowerMockito.when(completableFutureMock.get(1, TimeUnit.MILLISECONDS)).thenThrow(new TimeoutException());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final int result = bridgeClientUnderTest.loginSync(deviceId, password, 1);
        assertEquals(-1, result);
    }

    /**
     * 用例编号:test_report_properties_success
     * 用例标题:网桥设备发布属性成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报设备属性
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_report_properties_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.reportProperties(deviceId, Collections.singletonList(new ServiceProperty()), null);
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    /**
     * 用例编号:test_reset_secret_success
     * 用例标题:网桥设备发布重置密钥成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报重置密钥
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_reset_secret_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.resetSecret(deviceId, requestId, new DeviceSecret("oldSecret", "newSecret"), null);
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    /**
     * 用例编号:test_report_device_message_success
     * 用例标题:网桥设备发布设备消息成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报设备消息
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_report_device_message_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.reportDeviceMessage(deviceId, new DeviceMessage(), null);
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    /**
     * 用例编号:test_report_event_success
     * 用例标题:网桥设备发布设备事件成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报设备事件
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_report_event_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.reportEvent(deviceId, new DeviceEvent(), null);
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    /**
     * 用例编号:test_respond_command_success
     * 用例标题:网桥设备发布设备命令响应结果成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报设备命令响应结果
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_respond_command_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.respondCommand(deviceId, requestId, new CommandRsp(0));
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    /**
     * 用例编号:test_respond_prop_get_success
     * 用例标题:网桥设备发布设备属性查询结果成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报设备属性查询结果
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_respond_prop_get_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.respondPropsGet(deviceId, requestId, Collections.singletonList(new ServiceProperty()));
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    /**
     * 用例编号:test_respond_prop_set_success
     * 用例标题:网桥设备发布设备属性设置结果成功
     * 用例级别:Level 1
     * 预置条件:网桥已连上平台，正确的网桥设备Id和密钥
     * 测试步骤:网桥上报设备属性设置结果
     * 预期结果:connect链接成功调用一次发布接口
     * 修改记录:2022/08/27 
     */
    @Test
    public void test_respond_prop_set_success() {
        PowerMockito.doNothing().when(connMock).publishMessage(any(RawMessage.class), eq(null));
        bridgeClientUnderTest.respondPropsSet(deviceId, requestId, new IotResult(0, "success"));
        verify(connMock).publishMessage(any(RawMessage.class), eq(null));
    }

    private static class BridgeClientForTest extends BridgeClient {
        BridgeClientForTest(Connection connection, ClientConf clientConf, AbstractDevice device) {
            super(clientConf, device);
            super.connection = connection;
        }
    }

}
