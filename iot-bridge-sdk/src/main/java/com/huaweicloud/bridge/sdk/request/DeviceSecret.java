package com.huaweicloud.bridge.sdk.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceSecret {

    @JsonProperty("old_secret")
    private String oldSecret;

    @JsonProperty("new_secret")
    private String newSecret;

    public DeviceSecret(String oldSecret, String newSecret) {
        this.oldSecret = oldSecret;
        this.newSecret = newSecret;
    }

    public String getOldSecret() {
        return oldSecret;
    }

    public void setOldSecret(String oldSecret) {
        this.oldSecret = oldSecret;
    }

    public String getNewSecret() {
        return newSecret;
    }

    public void setNewSecret(String newSecret) {
        this.newSecret = newSecret;
    }
}
