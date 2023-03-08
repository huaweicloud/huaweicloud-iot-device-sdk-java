package com.huaweicloud.sdk.iot.device.filemanager;

import com.huaweicloud.sdk.iot.device.filemanager.response.UrlResponse;

/**
 * 监听文件上传下载事件
 */
public interface FileMangerListener {
    /**
     * 接收文件上传url
     *
     * @param param 上传参数
     */
    void onUploadUrl(UrlResponse param);

    /**
     * 接收文件下载url
     *
     * @param param 下载参数
     */
    void onDownloadUrl(UrlResponse param);
}
