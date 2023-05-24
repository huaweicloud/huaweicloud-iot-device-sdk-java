package com.huaweicloud.sdk.iot.device.ota;

public class OTAPackageV2 {
    private String url;

    private String version;

    private int expires;


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

    public int getExpires() {
        return expires;
    }

    public void setExpires(int expires) {
        this.expires = expires;
    }


    @Override
    public String toString() {
        return "OTAPackageV2{" + "url='" + url + '\'' + ", version='" + version
                + '\'' + ", expires=" + expires + '\'' + '}';
    }
}
