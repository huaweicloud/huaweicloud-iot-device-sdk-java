package com.huaweicloud.sdk.iot.bridge.sample.tcp.dto;

/**
 * 设备位置信息
 */
public class DeviceLocationMessage extends BaseMessage {

    private String longitude;

    private String latitude;

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
