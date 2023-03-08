package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleBootstrapActionListener implements ActionListener {
    private static final Logger log = LogManager.getLogger(BootstrapSelfRegSample.class);

    private final BootstrapClient bootstrapClient;

    public SimpleBootstrapActionListener(BootstrapClient bootstrapClient) {
        this.bootstrapClient = bootstrapClient;
    }

    @Override
    public void onSuccess(Object context) {
        // 引导成功，获取到iot平台的地址
        String address = (String) context;
        log.info("bootstrap success, the address is {}", address);

        // 引导成功后关闭客户端
        bootstrapClient.close();

        // 与iot平台建立连接，上报消息
        IoTDevice device = bootstrapClient.getIoTDevice("ssl://" + address);
        if (device == null || device.init() != 0) {
            return;
        }
        device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
    }

    @Override
    public void onFailure(Object context, Throwable throwable) {
        // 引导失败
        log.error("bootstrap failed: {}", throwable.getMessage());
    }
}
