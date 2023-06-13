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
