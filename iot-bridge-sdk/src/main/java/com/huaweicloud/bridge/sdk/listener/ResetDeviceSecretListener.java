package com.huaweicloud.bridge.sdk.listener;

public interface ResetDeviceSecretListener {

    /**
     * 网桥设置设备密钥监听器
     *
     * @param deviceId   设备Id
     * @param requestId  请求Id
     * @param resultCode 结果码，0表示成功，其他表示失败。不带默认认为成功。
     * @param newSecret  设备新secret
     */
    void onResetDeviceSecret(String deviceId, String requestId, int resultCode, String newSecret);
}
