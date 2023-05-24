package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddSubDeviceFailedReason {
    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_msg")
    private String errorMsg;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "AddSubDeviceFailedReason{"
            + "nodeId='" + nodeId + '\''
            + ", productId='" + productId + '\''
            + ", errorCode='" + errorCode + '\''
            + ", errorMsg='" + errorMsg + '\''
            + '}';
    }
}
