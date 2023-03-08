package com.huaweicloud.sdk.iot.device.filemanager.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpFileStatusRequest {

    @JsonProperty("object_name")
    private String objectName;

    @JsonProperty("result_code")
    private Integer resultCode;

    @JsonProperty("status_code")
    private Integer statusCode;

    @JsonProperty("status_description")
    private String statusDescription;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        return "UploadFileStatusRequest{" + "objectName='" + objectName + '\'' + ", resultCode=" + resultCode
            + ", statusCode=" + statusCode + ", statusDescription='" + statusDescription + '\'' + '}';
    }
}
