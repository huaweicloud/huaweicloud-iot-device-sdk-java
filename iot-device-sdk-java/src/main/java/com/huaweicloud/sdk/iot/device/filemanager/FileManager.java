package com.huaweicloud.sdk.iot.device.filemanager;

import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FileManager extends AbstractService {
    private static final Logger log = LogManager.getLogger(FileManager.class);

    private FileMangerListener fileMangerListener;

    public FileManager() {
    }

    public FileManager(FileMangerListener fileMangerListener) {
        this.fileMangerListener = fileMangerListener;
    }

    /**
     * 获取文件上传url
     *
     * @param fileName 文件名
     */
    public void getUploadUrl(String fileName) {

        Map<String, Object> node = new HashMap<>();
        node.put("file_name", fileName);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("get_upload_url");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$file_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportEvent");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);

    }

    /**
     * 获取文件下载url
     *
     * @param fileName 下载文件名
     */
    public void getDownloadUrl(String fileName) {

        Map<String, Object> node = new HashMap<>();
        node.put("file_name", fileName);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("get_download_url");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$file_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportEvent");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);

    }

    /**
     * 接收文件处理事件
     *
     * @param deviceEvent 服务事件
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (fileMangerListener == null) {
            log.info("fileMangerListener is null");
            return;
        }

        if (deviceEvent.getEventType().equalsIgnoreCase("get_upload_url_response")) {
            UrlParam urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlParam.class);
            fileMangerListener.onUploadUrl(urlParam);
        } else if (deviceEvent.getEventType().equalsIgnoreCase("get_download_url_response")) {
            UrlParam urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlParam.class);
            fileMangerListener.onDownloadUrl(urlParam);
        }
    }

}
