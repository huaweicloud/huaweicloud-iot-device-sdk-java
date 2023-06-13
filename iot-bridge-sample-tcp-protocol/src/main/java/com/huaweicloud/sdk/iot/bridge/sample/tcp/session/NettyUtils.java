package com.huaweicloud.sdk.iot.bridge.sample.tcp.session;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public final class NettyUtils {

    private static final String ATTR_DEVICE_ID = "deviceId";

    private static final AttributeKey<Object> ATTR_KEY_DEVICE_ID = AttributeKey.valueOf(ATTR_DEVICE_ID);

    public static void setDeviceId(Channel channel, String deviceId) {
        channel.attr(NettyUtils.ATTR_KEY_DEVICE_ID).set(deviceId);
    }

    public static String getDeviceId(Channel channel) {
        return (String) channel.attr(NettyUtils.ATTR_KEY_DEVICE_ID).get();
    }
}
