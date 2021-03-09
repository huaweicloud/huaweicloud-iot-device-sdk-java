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

        return new StringBuilder().append("DeviceServiceCapability [serviceId=").append(serviceId)
                .append(", serviceType=").append(serviceType).append(", option=").append(option)
                .append(", description=").append(description).append(", lastModifyTime=").append(lastModifyTime)
                .append(", commands=").append(commands).append(", properties=").append(properties).append(", events=")
                .toString();

    }


}
