package com.huaweicloud.sdk.iot.device.filemanager;

/**
 * 监听文件上传下载事件
 */
public interface FileMangerListener {
    /**
     * 接收文件上传url
     *
     * @param param 上传参数
     */
    void onUploadUrl(UrlParam param);

    /**
     * 接收文件下载url
     *
     * @param param 下载参数
     */
    void onDownloadUrl(UrlParam param);
}
