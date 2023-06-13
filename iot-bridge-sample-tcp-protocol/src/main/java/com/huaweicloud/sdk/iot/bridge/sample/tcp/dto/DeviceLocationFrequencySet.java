package com.huaweicloud.sdk.iot.bridge.sample.tcp.dto;

/**
 * 位置上报周期
 */
public class DeviceLocationFrequencySet extends BaseMessage {

    private int period;

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

}
