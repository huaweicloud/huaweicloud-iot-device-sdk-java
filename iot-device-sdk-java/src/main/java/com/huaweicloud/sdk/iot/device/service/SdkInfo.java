package com.huaweicloud.sdk.iot.device.service;

/**
 * 此服务实现sdk信息
 */
public class SdkInfo extends AbstractService {

    @Property(writeable = false)
    private String type = "Java";

    @Property(writeable = false)
    private String version = "0.8.0";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
