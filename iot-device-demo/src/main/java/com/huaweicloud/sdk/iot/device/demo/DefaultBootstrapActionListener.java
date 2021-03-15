package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultBootstrapActionListener implements ActionListener {

    private static final Logger log = LogManager.getLogger(DefaultBootstrapActionListener.class);

    private String deviceId;

    private String secret;

    private BootstrapClient bootstrapClient;

    public DefaultBootstrapActionListener(String deviceId, String secret, BootstrapClient bootstrapClient) {
        this.deviceId = deviceId;
        this.secret = secret;
        this.bootstrapClient = bootstrapClient;
    }

    @Override
    public void onSuccess(Object context) {
        String address = (String) context;
        log.info("bootstrap success:" + address);

        //引导成功后关闭客户端
        bootstrapClient.close();
        IoTDevice device = new IoTDevice("ssl://" + address, deviceId, secret);
        if (device.init() != 0) {
            return;

        }

        //上报消息
        device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
    }

    @Override
    public void onFailure(Object context, Throwable var2) {
        log.error("bootstrap failed: {}", var2.getMessage());
    }
}
