package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

/**
 * 命令响应
 */
public class CommandRsp {

    public static final int SUCCESS = 0;
    public static final int FAIL = -1;

    /**
     * 结果码，0表示成功，其他表示失败。不带默认认为成功
     */
    @JsonProperty("result_code")
    int resultCode;

    /**
     * 命令的响应名称，在设备关联的产品模型中定义。可选
     */
    @JsonProperty("response_name")
    String responseName;

    /**
     * 命令的响应参数，具体字段在设备关联的产品模型中定义。可选
     */
    Object paras;

    public CommandRsp(int code) {
        resultCode = code;
    }

    public CommandRsp(int code, Object paras) {
        resultCode = code;
        this.paras = paras;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public Object getParas() {
        return paras;
    }

    public void setParas(Object paras) {
        this.paras = paras;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
