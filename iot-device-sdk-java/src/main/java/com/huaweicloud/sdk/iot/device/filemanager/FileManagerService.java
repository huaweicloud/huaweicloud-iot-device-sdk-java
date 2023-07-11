/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.filemanager;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.filemanager.request.UrlRequest;
import com.huaweicloud.sdk.iot.device.filemanager.request.OpFileStatusRequest;
import com.huaweicloud.sdk.iot.device.filemanager.response.UrlResponse;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传/下载服务类
 */
public class FileManagerService extends AbstractService {
    private static final Logger log = LogManager.getLogger(FileManagerService.class);

    private static final String FILE_NAME = "file_name";

    private static final String FILE_ATTRIBUTES = "file_attributes";

    private static final String FILE_MANAGER = "$file_manager";

    private static final String GET_UPLOAD_URL = "get_upload_url";

    private static final String GET_UPLOAD_URL_RESPONSE = "get_upload_url_response";

    private static final String OBJECT_NAME = "object_name";

    private static final String RESULT_CODE = "result_code";

    private static final String STATUS_CODE = "status_code";

    private static final String STATUS_DESCRIPTION = "status_description";

    private static final String UPLOAD_RESULT_REPORT = "upload_result_report";

    private static final String GET_DOWNLOAD_URL = "get_download_url";

    private static final String GET_DOWNLOAD_URL_RESPONSE = "get_download_url_response";

    private static final String DOWNLOAD_RESULT_REPORT = "download_result_report";

    private FileMangerListener fileMangerListener;

    private BridgeFileMangerListener bridgeFileMangerListener;

    /**
     * 设置设备获取文件上传URL监听器
     *
     * @param fileMangerListener 监听器
     */
    public void setFileMangerListener(FileMangerListener fileMangerListener) {
        this.fileMangerListener = fileMangerListener;
    }

    /**
     * 设置网桥获取文件上传URL监听器
     *
     * @param bridgeFileMangerListener 监听器
     */
    public void setBridgeFileMangerListener(
        BridgeFileMangerListener bridgeFileMangerListener) {
        this.bridgeFileMangerListener = bridgeFileMangerListener;
    }

    /**
     * 直连设备获取文件上传url
     *
     * @param gettingUpLoadUrlDTO 请求体
     * @param listener            监听器
     */
    public void getUploadUrl(UrlRequest gettingUpLoadUrlDTO, ActionListener listener) {
        DeviceEvent deviceEvent = generateUpOrDownUrlDeviceEvent(gettingUpLoadUrlDTO, GET_UPLOAD_URL);
        getIotDevice().getClient().reportEvent(deviceEvent, listener);
    }

    /**
     * 网桥获取文件上传url
     *
     * @param deviceId            设备Id
     * @param gettingUpLoadUrlDTO 请求体
     * @param listener            监听器
     */
    public void getUploadUrlOfBridge(String deviceId, UrlRequest gettingUpLoadUrlDTO,
        ActionListener listener) {
        DeviceEvent deviceEvent = generateUpOrDownUrlDeviceEvent(gettingUpLoadUrlDTO, GET_UPLOAD_URL);
        getIotDevice().getClient().reportEvent(deviceId, deviceEvent, listener);
    }

    private DeviceEvent generateUpOrDownUrlDeviceEvent(UrlRequest gettingUpLoadUrlDTO, String eventType) {
        Map<String, Object> node = new HashMap<>();
        node.put(FILE_NAME, gettingUpLoadUrlDTO.getFileName());
        node.put(FILE_ATTRIBUTES, gettingUpLoadUrlDTO.getFileAttributes());

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId(FILE_MANAGER);
        deviceEvent.setEventType(eventType);
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setParas(node);
        return deviceEvent;
    }

    private DeviceEvent generateUploadFileStatusEvent(OpFileStatusRequest uploadFileStatusRequest, String eventType) {
        Map<String, Object> node = new HashMap<>();
        node.put(OBJECT_NAME, uploadFileStatusRequest.getObjectName());
        node.put(RESULT_CODE, uploadFileStatusRequest.getResultCode());
        node.put(STATUS_CODE, uploadFileStatusRequest.getStatusCode());
        node.put(STATUS_DESCRIPTION, uploadFileStatusRequest.getStatusDescription());

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId(FILE_MANAGER);
        deviceEvent.setEventType(eventType);
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setParas(node);
        return deviceEvent;
    }

    /**
     * 直连设备上报文件上传结果
     *
     * @param uploadFileStatusRequest 文件上传结果
     * @param listener                监听器
     */
    public void reportUploadFileStatus(OpFileStatusRequest uploadFileStatusRequest, ActionListener listener) {
        DeviceEvent deviceEvent = generateUploadFileStatusEvent(uploadFileStatusRequest, UPLOAD_RESULT_REPORT);
        getIotDevice().getClient().reportEvent(deviceEvent, listener);
    }

    /**
     * 网桥上报文件上传结果
     *
     * @param deviceId                设备Id
     * @param uploadFileStatusRequest 文件上传结果
     * @param listener                监听器
     */
    public void reportUploadFileStatusOfBridge(String deviceId, OpFileStatusRequest uploadFileStatusRequest,
        ActionListener listener) {
        DeviceEvent deviceEvent = generateUploadFileStatusEvent(uploadFileStatusRequest, UPLOAD_RESULT_REPORT);
        getIotDevice().getClient().reportEvent(deviceId, deviceEvent, listener);
    }

    /**
     * 直连设备获取文件下载URL
     *
     * @param urlRequest 请求体
     * @param listener   监听器
     */
    public void getDownloadUrl(UrlRequest urlRequest, ActionListener listener) {
        DeviceEvent deviceEvent = generateUpOrDownUrlDeviceEvent(urlRequest, GET_DOWNLOAD_URL);
        getIotDevice().getClient().reportEvent(deviceEvent, listener);
    }

    /**
     * 网桥设备获取文件下载URL
     *
     * @param deviceId   设备Id
     * @param urlRequest 请求体
     * @param listener   监听器
     */
    public void getDownloadUrlOfBridge(String deviceId, UrlRequest urlRequest, ActionListener listener) {
        DeviceEvent deviceEvent = generateUpOrDownUrlDeviceEvent(urlRequest, GET_DOWNLOAD_URL);
        getIotDevice().getClient().reportEvent(deviceId, deviceEvent, listener);
    }

    /**
     * 直连设备上报文件下载结果
     *
     * @param uploadFileStatusRequest 请求体
     * @param listener                监听器
     */
    public void reportDownloadFileStatus(OpFileStatusRequest uploadFileStatusRequest, ActionListener listener) {
        DeviceEvent deviceEvent = generateUploadFileStatusEvent(uploadFileStatusRequest, DOWNLOAD_RESULT_REPORT);
        getIotDevice().getClient().reportEvent(deviceEvent, listener);
    }

    /**
     * 直连设备上报文件下载结果
     *
     * @param deviceId                设备Id
     * @param uploadFileStatusRequest 请求体
     * @param listener                监听器
     */
    public void reportDownloadFileStatusOfBridge(String deviceId, OpFileStatusRequest uploadFileStatusRequest,
        ActionListener listener) {
        DeviceEvent deviceEvent = generateUploadFileStatusEvent(uploadFileStatusRequest, DOWNLOAD_RESULT_REPORT);
        getIotDevice().getClient().reportEvent(deviceId, deviceEvent, listener);
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

        if (deviceEvent.getEventType().equalsIgnoreCase(GET_UPLOAD_URL_RESPONSE)) {
            UrlResponse urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlResponse.class);
            fileMangerListener.onUploadUrl(urlParam);
        } else if (deviceEvent.getEventType().equalsIgnoreCase(GET_DOWNLOAD_URL_RESPONSE)) {
            UrlResponse urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlResponse.class);
            fileMangerListener.onDownloadUrl(urlParam);
        } else {
            log.error("invalid event type.");
        }
    }

    /**
     * 网桥场景下接受文件处理事件
     *
     * @param deviceId    设备Id
     * @param deviceEvent 服务事件
     */
    @Override
    public void onBridgeEvent(String deviceId, DeviceEvent deviceEvent) {
        if (bridgeFileMangerListener == null) {
            log.info("bridgeFileMangerListener is null");
            return;
        }

        if (deviceEvent.getEventType().equalsIgnoreCase(GET_UPLOAD_URL_RESPONSE)) {
            UrlResponse urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlResponse.class);
            bridgeFileMangerListener.onUploadUrl(urlParam, deviceId);
        } else if (deviceEvent.getEventType().equalsIgnoreCase(GET_DOWNLOAD_URL_RESPONSE)) {
            UrlResponse urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlResponse.class);
            bridgeFileMangerListener.onDownloadUrl(urlParam, deviceId);
        } else {
            log.error("invalid event type.");
        }
    }
}
