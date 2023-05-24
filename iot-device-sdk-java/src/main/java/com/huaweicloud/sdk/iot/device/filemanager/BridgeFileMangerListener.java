package com.huaweicloud.sdk.iot.device.filemanager;

import com.huaweicloud.sdk.iot.device.filemanager.response.UrlResponse;

/**
 * 监听文件上传下载事件
 */
public interface BridgeFileMangerListener {
    /**
     * 接收文件上传url
     *
     * @param param    上传参数
     * @param deviceId 设备Id
     */
    void onUploadUrl(UrlResponse param, String deviceId);

    /**
     * 接收文件下载url
     *
     * @param param    下载参数`
     * @param deviceId 设备Id
     */
    void onDownloadUrl(UrlResponse param, String deviceId);
}
