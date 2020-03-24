package com.huaweicloud.sdk.iot.device.filemanager;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 未完成
 */
public class FileManager extends AbstractService {


    private Logger log = Logger.getLogger(this.getClass());
    private FileMangerListener fileMangerListener;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        getIotDevice().getClient().reportEvent(deviceEvent, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportEvent failed: " + var2.getMessage());
            }
        });

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

        getIotDevice().getClient().reportEvent(deviceEvent, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportEvent failed: " + var2.getMessage());
            }
        });

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
