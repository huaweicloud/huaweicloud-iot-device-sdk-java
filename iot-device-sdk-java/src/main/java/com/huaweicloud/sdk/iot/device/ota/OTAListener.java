package com.huaweicloud.sdk.iot.device.ota;

/**
 * OTA监听器
 */
public interface OTAListener {
    /**
     * 接收查询版本通知
     */
    void onQueryVersion();

    /**
     * 接收新版本通知
     *
     * @param pkg 新版本包信息
     */
    void onNewPackage(OTAPackage pkg);

    /**
     * 接收V2新版本通知
     *
     * @param pkg 新版本包信息
     */
    void onNewPackageV2(OTAPackageV2 pkg);
}
