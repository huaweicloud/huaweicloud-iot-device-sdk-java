package com.huaweicloud.sdk.iot.device.gateway.requests;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 设备信息
 */
public class DeviceInfo {

    @JsonProperty("parent_device_id")
    String parent;

    @JsonProperty("node_id")
    String nodeId;

    @JsonProperty("device_id")
    String deviceId;

    String name;
    String description;

    @JsonProperty("manufacturer_id")
    String manufacturerId;

    String model;

    @JsonProperty("product_id")
    String productId;

    @JsonProperty("fw_version")
    String fwVersion;

    @JsonProperty("sw_version")
    String swVersion;

    String status;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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

    @Override
    public String toString() {
        return "DeviceInfo{"
                + "parent='" + parent + '\''
                + ", nodeId='" + nodeId + '\''
                + ", deviceId='" + deviceId + '\''
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", manufacturerId='" + manufacturerId + '\''
                + ", model='" + model + '\''
                + ", productId='" + productId + '\''
                + ", fwVersion='" + fwVersion + '\''
                + ", swVersion='" + swVersion + '\''
                + ", status='" + status + '\''
                + '}';
    }
}
