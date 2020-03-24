package com.huaweicloud.sdk.iot.device.filemanager;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UrlParam {

    String url;

    @JsonProperty("bucket_name")
    String bucketName;

    @JsonProperty("object_name")
    String objectName;

    Integer expire;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }
}
