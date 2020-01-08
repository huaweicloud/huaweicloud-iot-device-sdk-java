package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 扫描子设备通知
 */
public class ScanSubdeviceNotify {
    String protocol;
    String channel;

    @JsonProperty("parent_device_id")
    String parent;

    @JsonProperty("scan_settings")
    ObjectNode settings;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ObjectNode getSettings() {
        return settings;
    }

    public void setSettings(ObjectNode settings) {
        this.settings = settings;
    }
}
