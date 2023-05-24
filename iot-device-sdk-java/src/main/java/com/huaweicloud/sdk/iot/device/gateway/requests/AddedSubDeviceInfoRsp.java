package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddedSubDeviceInfoRsp {
    @JsonProperty("parent_device_id")
    private String parentDeviceId;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("device_id")
    private String deviceId;

    private String name;

    private String description;

    @JsonProperty("manufacturer_id")
    private String manufacturerId;

    private String model;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("fw_version")
    private String fwVersion;

    @JsonProperty("sw_version")
    private String swVersion;

    private String status;

    @JsonProperty("extension_info")
    private Object extensionInfo;

    public String getParentDeviceId() {
        return parentDeviceId;
    }

    public void setParentDeviceId(String parentDeviceId) {
        this.parentDeviceId = parentDeviceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getExtensionInfo() {
        return extensionInfo;
    }

    public void setExtensionInfo(Object extensionInfo) {
        this.extensionInfo = extensionInfo;
    }

    @Override
    public String toString() {
        return "AddedSubDeviceInfoRsp{"
            + "parentDeviceId='" + parentDeviceId
            + '\'' + ", nodeId='" + nodeId + '\''
            + ", deviceId='" + deviceId + '\''
            + ", name='" + name + '\''
            + ", description='" + description
            + '\'' + ", manufacturerId='"
            + manufacturerId + '\'' + ", model='"
            + model + '\'' + ", productId='"
            + productId + '\'' + ", fwVersion='"
            + fwVersion + '\'' + ", swVersion='"
            + swVersion + '\'' + ", status='"
            + status + '\'' + ", extensionInfo="
            + extensionInfo + '}';
    }
}
