package com.huaweicloud.sdk.iot.device.codegenerator.productparser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.List;


public class ServiceProperty implements Serializable {

    public static final String READ = "R";

    public static final String WRITE = "W";

    public static final String EXECUTE = "E";

    private static final long serialVersionUID = 8483808607881241605L;


    private String propertyName;

    //@Pattern(regexp = "(int|long|decimal|string|DateTime|jsonObject|enum|boolean|string list)")
    private String dataType;

    @JsonIgnore
    private String javaType;

    @JsonIgnore
    private String writeable;

    @JsonIgnore
    private String val;

    private boolean required;

    private String min;

    private String max;

    private double step;

    @JsonSetter(nulls = Nulls.SKIP)
    private int maxLength = 1024;

    // 访问方法：RWE 可读R，可写W，可观察E

    //@Pattern(regexp = "(RWE|RW|RE|WE|E|W|R)")
    private String method;

    private String unit;


    private List<String> enumList;

    /**
     * 描述
     */
    private String description;

    private transient ObjectNode extendparam;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public ObjectNode getExtendparam() {
        return extendparam;
    }

    public void setExtendparam(ObjectNode extendparam) {
        this.extendparam = extendparam;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getWriteable() {
        return writeable;
    }

    public void setWriteable(String writeable) {
        this.writeable = writeable;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ServiceProperty [propertyName=").append(propertyName).append(", dataType=")
                .append(dataType).append(", required=").append(required).append(", min=").append(min).append(", max=")
                .append(max).append(", step=").append(step).append(", maxLength=").append(maxLength).append(", method=")
                .append(method).append(", unit=").append(unit).append(", enumList=").append(enumList).append(", description=")
                .append(description).append("]")
                .toString();

    }


}

