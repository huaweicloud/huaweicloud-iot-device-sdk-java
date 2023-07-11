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

import java.io.Serializable;
import java.util.List;


/**
 * 设备服务能力
 */
public class DeviceService implements Serializable {

    private static final long serialVersionUID = -2045537649692709978L;

    /**
     * 服务Id
     * 如果设备中同类型的服务类型只有一个则serviceId与serviceType相同， 如果有多个则增加编号，如三键开关 Switch01、Switch02、Switch03。
     */
    private String serviceId;

    /**
     * 服务类型
     * 与servicetype-capability.json中serviceType字段保持一致。
     */
    private String serviceType;

    /**
     * 标识服务字段类型
     * Master（主服务）, Mandatory（必选服务）, Optional（可选服务）
     * 目前本字段为非功能性字段，仅起到描述作用
     */
    private String option;

    /**
     * 描述
     */
    private String description;

    /**
     * 上次更新时间
     */
    private String lastModifyTime;

    /**
     * 指示设备可以执行的命令
     */
    private List<ServiceCommand> commands;

    /**
     * 服务属性
     * 用于设备影子、属性类型判断等
     */
    private List<ServiceProperty> properties;

    private List<Object> events;


    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(String lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    public List<ServiceCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<ServiceCommand> commands) {
        this.commands = commands;
    }

    public List<ServiceProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ServiceProperty> properties) {
        this.properties = properties;
    }


    public List<Object> getEvents() {
        return events;
    }

    public void setEvents(List<Object> events) {
        this.events = events;
    }

    @Override
    public String toString() {

        return "DeviceServiceCapability [serviceId=" + serviceId
            + ", serviceType=" + serviceType + ", option=" + option
            + ", description=" + description + ", lastModifyTime=" + lastModifyTime
            + ", commands=" + commands + ", properties=" + properties + ", events=]";

    }


}
