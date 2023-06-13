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
