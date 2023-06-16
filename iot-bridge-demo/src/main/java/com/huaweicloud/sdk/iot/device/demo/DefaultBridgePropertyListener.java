package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import io.netty.channel.Channel;

public class DefaultBridgePropertyListener implements PropertyListener {

    private static final Logger log = LogManager.getLogger(DefaultBridgePropertyListener.class);

    private Channel channel;

    private IoTDevice ioTDevice;

    DefaultBridgePropertyListener(Channel channel, IoTDevice ioTDevice) {
        this.channel = channel;
        this.ioTDevice = ioTDevice;
    }

    @Override
    public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

        // 这里可以根据需要进行消息格式转换
        channel.writeAndFlush(services);
        ioTDevice.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
    }

    @Override
    public void onPropertiesGet(String requestId, String serviceId) {

        log.error("not supporte onSubdevPropertiesGet");
        ioTDevice.getClient().respondPropsSet(requestId, IotResult.FAIL);
    }
}
