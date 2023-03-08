package com.huaweicloud.sdk.iot.device.filemanager.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class UrlRequest {
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_attributes")
    private Map<String, Object> fileAttributes;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Object> getFileAttributes() {
        return fileAttributes;
    }

    public void setFileAttributes(Map<String, Object> fileAttributes) {
        this.fileAttributes = fileAttributes;
    }

    @Override
    public String toString() {
        return "UrlRequest{" + "fileName='" + fileName + '\'' + ", fileAttributes=" + fileAttributes + '}';
    }
}
