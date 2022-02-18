package com.huaweicloud.sdk.iot.device.ota;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OTAPackage {
    private String url;

    private String version;

    @JsonProperty("file_size")
    private int fileSize;

    @JsonProperty("access_token")
    private String token;

    private int expires;

    private String sign;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "OTAPackage{" + "url='" + url + '\'' + ", version='" + version
            + '\'' + ", fileSize=" + fileSize + ", token='" + token + '\''
            + ", expires=" + expires + ", sign='" + sign + '\'' + '}';
    }
}
