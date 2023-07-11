/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.codegenerator.productparser;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.List;

public class DeviceCapability implements Serializable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_APP_ID = "appId";
    public static final String FIELD_MANUFACTURER_ID = "manufacturerId";
    public static final String FIELD_MODEL = "model";
    public static final String FIELD_PROTOCOL_TYPE = "protocolType";
    public static final String FIELD_DEVICE_TYPE = "deviceType";
    public static final String FIELD_MANUFACTURER_NAME = "manufacturerName";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_IOCN = "icon";
    public static final String FIELD_OM_CAPABILITY = "omCapability";
    public static final String FIELD_SERVICE_CAPABILITIES = "serviceTypeCapabilities";
    public static final String FIELD_LAST_MODIFY_TIME = "lastModifyTime";
    public static final String FIELD_PRODUCTID = "productId";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_NEWFLAG = "newFlag";
    public static final String FIELD_NODETYPE = "nodeType";

    private static final long serialVersionUID = -6552611626281802652L;

    private String id;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 厂商Id
     */
    private String manufacturerId;

    /**
     * 厂商名
     */
    private String manufacturerName;

    /**
     * 设备的型号
     */
    private String model;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 描述
     */
    private String description;

    /**
     * 定制能力
     * 目前dm使用，定义设备的软件升级、固件升级和配置更新的能力
     */
    private ObjectNode omCapability;

    /**
     * 图标
     */
    private String icon;

    /**
     * 最后更新时间
     */
    private String lastModifyTime;

    /**
     * 服务能力
     */
    private List<DeviceService> serviceTypeCapabilities;

    /**
     * 版本
     */
    private String version;

    private boolean newFlag;

    /**
     * 产品Id
     */
    private String productId;

    /**
     * 节点类型，该字段暂时不用，保留
     */
    private String nodeType;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DeviceService> getServiceTypeCapabilities() {
        return serviceTypeCapabilities;
    }

    public void setServiceTypeCapabilities(List<DeviceService> serviceTypeCapabilities) {
        this.serviceTypeCapabilities = serviceTypeCapabilities;
    }

    public ObjectNode getOmCapability() {
        return omCapability;
    }

    public void setOmCapability(ObjectNode omCapability) {
        this.omCapability = omCapability;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isNewFlag() {
        return newFlag;
    }

    public void setNewFlag(boolean newFlag) {
        this.newFlag = newFlag;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        String builder = "DeviceCapability [appId="
            + appId
            + ", deviceType="
            + deviceType
            + ", manufacturerId="
            + manufacturerId
            + ", manufacturerName="
            + manufacturerName
            + ", model="
            + model
            + ", protocolType="
            + protocolType
            + ", nodeType="
            + nodeType
            + ", description="
            + description
            + ", omCapability="
            + omCapability
            + ", icon="
            + icon
            + ", lastModifyTime="
            + lastModifyTime
            + ", serviceTypeCapabilities="
            + serviceTypeCapabilities
            + "]";
        return builder;
    }

}
