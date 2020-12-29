package com.huaweicloud.sdk.iot.device.codegenerator.productparser;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.List;

public class ServiceCommandPara implements Serializable {
    private static final long serialVersionUID = 4565399257931903508L;

    private String paraName;

    private String dataType;

    private boolean required;

    private String min;

    private String max;

    private double step;

    private int maxLength;

    private String unit;

    private List<String> enumList;

    private ObjectNode extendParam;

    /**
     * 描述
     */

    private String description;

    public String getParaName() {
        return paraName;
    }

    public void setParaName(String paraName) {
        this.paraName = paraName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<String> getEnumList() {
        return enumList;
    }

    public void setEnumList(List<String> enumList) {
        this.enumList = enumList;
    }

    public ObjectNode getExtendParam() {
        return extendParam;
    }

    public void setExtendParam(ObjectNode extendParam) {
        this.extendParam = extendParam;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ServiceCommandPara [paraName=").append(paraName).append(", dataType=")
            .append(dataType).append(", required=").append(required).append(", min=").append(min).append(", max=")
            .append(max).append(", step=").append(step).append(", maxLength=").append(maxLength).append(", unit=")
            .append(unit).append(", enumList=").append(enumList).append(", description=")
            .append(description).append("]").toString();

    }

}

