package com.huaweicloud.sdk.iot.device.constants;

public class Constants {

    /**
     * 直连设备接入模式
     */
    public static final int CONNECT_OF_NORMAL_DEVICE_MODE = 0;

    /**
     * 网桥模式接入模式
     */
    public static final int CONNECT_OF_BRIDGE_MODE = 3;

    /**
     * 时间戳校验模式
     */
    public static final int CHECK_STAMP_SHA256_OFF = 0; // HMAC-SHA256不校验时间戳

    public static final int CHECK_STAMP_SHA256_ON = 1; // HMAC-SHA256校验时间戳

    public static final int CHECK_STAMP_SM3_ON = 2; // HMAC-SM3校验时间戳

}
