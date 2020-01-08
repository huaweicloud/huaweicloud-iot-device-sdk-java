package com.huaweicloud.sdk.iot.device.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

/**
 * 处理结果
 */
public class IotResult {

    public final static IotResult SUCCESS = new IotResult(0, "Success");
    public final static IotResult FAIL = new IotResult(1, "Fail");
    public final static IotResult TIMEOUT = new IotResult(2, "Timeout");


    /**
     * 结果码，0表示成功，其他为失败
     */
    @JsonProperty("result_code")
    private int resultCode;

    /**
     * 结果描述
     */
    @JsonProperty("result_desc")
    private String resultDesc;


    /**
     * 处理结果
     *
     * @param resultCode 结果码
     * @param resultDesc 结果描述
     */
    public IotResult(int resultCode, String resultDesc) {
        this.resultCode = resultCode;
        this.resultDesc = resultDesc;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
