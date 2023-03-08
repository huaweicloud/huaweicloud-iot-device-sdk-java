package com.huaweicloud.sdk.iot.device.filemanager.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class UrlResponse {
    private String url;

    @JsonProperty("bucket_name")
    private String bucketName;

    @JsonProperty("object_name")
    private String objectName;

    @JsonProperty("file_attributes")
    private Map<String, Object> fileAttributes;

    private Integer expire;

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

    public Map<String, Object> getFileAttributes() {
        return fileAttributes;
    }

    public void setFileAttributes(Map<String, Object> fileAttributes) {
        this.fileAttributes = fileAttributes;
    }

    @Override
    public String toString() {
        return "UrlParam{" + "url='" + url + '\'' + ", bucketName='" + bucketName + '\'' + ", objectName='" + objectName
            + '\'' + ", expire=" + expire + ", fileAttributes='" + fileAttributes + '\'' + '}';
    }
}
