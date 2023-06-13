package com.huaweicloud.bridge.sdk.listener;

import java.util.Map;

public interface LogoutListener {
    /**
     * 网桥下设备登出监听器
     *
     * @param deviceId  设备Id
     * @param requestId 请求Id
     * @param map       响应体
     */
    void onLogout(String deviceId, String requestId, Map<String, Object> map);
}
