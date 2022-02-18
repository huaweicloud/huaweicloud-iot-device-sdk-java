package com.huaweicloud.sdk.iot.device.client;

/**
 * 请求监听器
 */
public interface RequestListener {
    /**
     * 请求执行完成通知
     *
     * @param result 请求执行结果
     */
    void onFinish(String result);
}
