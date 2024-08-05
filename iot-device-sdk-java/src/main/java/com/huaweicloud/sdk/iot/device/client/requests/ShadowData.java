package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

public class ShadowData {
    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("desired")
    private PropertiesData desired;

    @JsonProperty("reported")
    private PropertiesData reported;

    @JsonProperty("version")
    private Integer version;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public PropertiesData getDesired() {
        return desired;
    }

    public void setDesired(PropertiesData desired) {
        this.desired = desired;
    }

    public PropertiesData getReported() {
        return reported;
    }

    public void setReported(PropertiesData reported) {
        this.reported = reported;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
