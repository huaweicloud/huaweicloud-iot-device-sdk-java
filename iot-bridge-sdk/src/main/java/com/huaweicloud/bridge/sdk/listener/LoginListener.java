package com.huaweicloud.bridge.sdk.listener;

public interface LoginListener {

    /**
     * 网桥下设备登录监听器
     *
     * @param deviceId   设备Id
     * @param requestId  请求Id
     * @param resultCode 登录响应码
     */
    void onLogin(String deviceId, String requestId, int resultCode);

}
