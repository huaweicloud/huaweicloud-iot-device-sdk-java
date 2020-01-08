package com.huaweicloud.sdk.iot.device.ota;

/**
 * OTA监听器
 */
public interface OTAListener {

    /**
     * 接收查询版本通知
     */
    public void onQueryVersion();

    /**
     * 接收新版本通知
     *
     * @param pkg 新版本包信息
     */
    public void onNewPackage(OTAPackage pkg);
}
