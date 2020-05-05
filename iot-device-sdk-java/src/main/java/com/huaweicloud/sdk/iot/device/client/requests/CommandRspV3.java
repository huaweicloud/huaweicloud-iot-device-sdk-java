package com.huaweicloud.sdk.iot.device.client.requests;

import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

/**
 * 命令响应V3
 */
public class CommandRspV3 {

    /**
     * 消息类型 固定值为：deviceRsp
     */
    private String msgType;

    /**
     * 命令ID，把物联网平台下发命令时携带的“mid”返回给平台。
     */
    private int mid;

    /**
     * 命令执行的结果码:
     *  “0”表示执行成功。
     *  “1”表示执行失败。
     */
    private int errcode;

    /**
     * 命令的应答，具体字段在设备的产品模型中定义，可选。
     */
    private Object paras;

    public CommandRspV3(String msgType, int mid, int errcode) {
        this.msgType = msgType;
        this.mid = mid;
        this.errcode = errcode;
    }

    public CommandRspV3(String msgType, int mid, int errcode, Object paras) {
        this.msgType = msgType;
        this.mid = mid;
        this.errcode = errcode;
        this.paras = paras;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
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
