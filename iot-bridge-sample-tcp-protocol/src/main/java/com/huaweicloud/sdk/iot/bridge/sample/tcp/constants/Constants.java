package com.huaweicloud.sdk.iot.bridge.sample.tcp.constants;

public class Constants {

    // 登录消息
    public static final String MSG_TYPE_DEVICE_LOGIN = "DEVICE_LOGIN";

    // 位置上报
    public static final String MSG_TYPE_REPORT_LOCATION_INFO = "REPORT_LOCATION_INFO";

    // 设备位置上报周期
    public static final String MSG_TYPE_FREQUENCY_LOCATION_SET = "FREQUENCY_LOCATION_SET";


    // 设备请求
    public static final int DIRECT_DEVICE_REQ = 3;

    // 云端响应消息
    public static final int DIRECT_CLOUD_RSP = 4;

    // 云端发给设备的消息
    public static final int DIRECT_CLOUD_REQ = 1;

    // 设备返回的响应消息
    public static final int DIRECT_DEVICE_RSP = 2;

    // 消息头分隔符
    public static final String HEADER_PARS_DELIMITER  = ",";

    // 消息体分隔符
    public static final String BODY_PARS_DELIMITER = "@";

    // 消息开始标志
    public static final String MESSAGE_START_DELIMITER = "[";

    // 消息结束标志
    public static final String MESSAGE_END_DELIMITER = "]";

}
