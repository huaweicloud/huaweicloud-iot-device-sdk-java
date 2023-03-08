package com.huaweicloud.sdk.iot.device.transport;


/**
 * 动作监听器，用户接收动作执行结果
 */
public interface ActionListener {
    /**
     * 执行成功通知
     *
     * @param context 上下文信息
     */
    void onSuccess(Object context);

    /**
     * 执行失败通知
     *
     * @param context 上下文信息
     * @param var2    失败的原因
     */
    void onFailure(Object context, Throwable var2);
}
