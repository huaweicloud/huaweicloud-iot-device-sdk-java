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

package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.bridge.sdk.BridgeDevice;
import com.huaweicloud.bridge.sdk.constants.BridgeSDKConstants;
import com.huaweicloud.bridge.sdk.listener.BridgePropertyListener;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.bridge.sdk.request.DeviceSecret;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.constants.Constants;
import com.huaweicloud.sdk.iot.device.filemanager.BridgeFileMangerListener;
import com.huaweicloud.sdk.iot.device.filemanager.FileManagerService;
import com.huaweicloud.sdk.iot.device.filemanager.request.OpFileStatusRequest;
import com.huaweicloud.sdk.iot.device.filemanager.request.UrlRequest;
import com.huaweicloud.sdk.iot.device.filemanager.response.UrlResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class BridgeSample {

    private static final Logger log = LogManager.getLogger(BridgeSample.class);

    /**
     * iot平台连接地址
     */
    private static final String SERVER_URI
        = "[Please input server uri here, example: ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883]";

    /**
     * 网桥设备Id（需要提前在iot平台上注册）
     */
    private static final String BRIDGE_ID
        = "[Please input your bridge id here, example:702b1038-a174-4a1d-969f-f67f8df43c4a]";

    /**
     * 设备密钥
     */
    private static final String BRIDGE_SECRET = "[Please input your bridge secret here, example:myBridgeSecret]";

    /**
     * 网桥下的设备Id
     */
    private static final String DEVICE_ID
        = "[Please input your device id here, example:myDeviceId]";

    /**
     * 网桥下设备的密钥
     */
    private static final String DEVICE_SECRET = "[Please input your device secret here, example:myDeviceSecret]";

    /**
     * 设备新的密钥，重置设备密钥时使用
     */
    private static final String NEW_DEVICE_SECRET
        = "[Please input your new device secret here, example:myNewDeviceSecret]";

    /**
     * 请求Id，用于标识上报的消息, 每条需要的Id建议保持不一致
     */
    private static final String REQ_ID = "[Please input your request id here, example: myRequestId]";

    /**
     * 服务Id，需要跟物模型中设置的一致
     */
    private static final String SERVICE_ID = "[Please input your service id here, example: BasicData]";

    /**
     * 属性，需要跟物模型中设置的一致。
     */
    private static final String PROPERTY = "[Please input your property here, example: luminance]";

    /**
     * 消息名称， 建议每条消息不一致，用于消息上报。
     */
    private static final String MESSAGE_NAME = "[Please input your message name here, example: messageName]";

    /**
     * 消息Id， 建议每条消息不一致，用于消息上报。
     */
    private static final String MESSAGE_ID = "[Please input your message id here, example: messageId]";

    /**
     * 消息内容， 建议每条消息不一致，用于消息上报。
     */
    private static final String MESSAGE_CONTENT = "[Please input your message content here, example: messageContent]";

    /**
     * 上传文件结果码，此处样例填写0。
     */
    private static final int RESULT_CODE_OF_FILE_UP = 0;

    /**
     * 文件哈希值
     */
    private static final String HASH_CODE
        = "[Please input your hash code here, example: 58059181f378062f9b446e884362a526]";

    /**
     * 文件名称，此处样例填写a.jpg。
     */
    private static final String FILE_NAME = "[Please input your message content here, example: a.jpg]";

    /**
     * 文件大小，此处样例填写1024。
     */
    private static final int SIZE = 1024;

    public static void main(String[] args) throws InterruptedException, TimeoutException, ExecutionException {
        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(SERVER_URI);
        clientConf.setDeviceId(BRIDGE_ID);
        clientConf.setSecret(BRIDGE_SECRET);
        clientConf.setMode(Constants.CONNECT_OF_BRIDGE_MODE);

        BridgeDevice bridgeDevice = BridgeDevice.getInstance(clientConf);
        if (bridgeDevice.init() < 0) {
            log.warn("the bridge connect error");
            return;
        }

        /**
         * 网桥设备同步登录接口。
         */
        int result = bridgeDevice.getClient().loginSync(DEVICE_ID, BRIDGE_SECRET, 1000);
        if (result != 0) {
            log.warn("bridge device login failed. the result is {}", result);
            return;
        }

        // 上报属性
        reportProperty(bridgeDevice);

        // 上报设备消息
        reportDeviceMessage(bridgeDevice);

        // 处理命令下发
        handleCommand(bridgeDevice);

        // 处理消息下发
        handlerMessageDown(bridgeDevice);

        // 重置设备密钥
        resetDeviceSecret(bridgeDevice);

        // 设备登出
        logout(bridgeDevice);

        // 设备断链
        disconnect(bridgeDevice);

        // 文件上传/下载功能
        uploadAndDownloadFile(bridgeDevice);

        // 属性设置/查询功能
        handlePropSetOrGet(bridgeDevice);

        /**
         * 网桥设备同步登出接口。
         */
        int ret = bridgeDevice.getClient().logoutSync(DEVICE_ID, 1000);
        if (ret != 0) {
            log.warn("bridge device logout failed. the result is {}", result);
        }

    }

    private static void handlePropSetOrGet(BridgeDevice bridgeDevice) {
        bridgeDevice.getClient().setBridgePropertyListener(new BridgePropertyListener() {
            @Override
            public void onPropertiesSet(String deviceId, String requestId, List<ServiceProperty> services) {
                log.info("the requestId is {}", requestId);
                log.info("the deviceId is {}", deviceId);

                if (Objects.isNull(services)) {
                    log.warn("the services is null");
                }

                // 遍历service
                for (ServiceProperty serviceProperty : services) {
                    log.info("OnPropertiesSet, serviceId is {}", serviceProperty.getServiceId());
                    // 遍历属性
                    for (String name : serviceProperty.getProperties().keySet()) {
                        log.info("property name is {}", name);
                        log.info("set property value is {}", serviceProperty.getProperties().get(name));
                    }

                }

                // 修改本地的属性值
                bridgeDevice.getClient().respondPropsSet(deviceId, requestId, IotResult.SUCCESS);
            }

            @Override
            public void onPropertiesGet(String deviceId, String requestId, String serviceId) {
                log.info("the requestId is {}", requestId);
                log.info("the deviceId is {}", deviceId);
                log.info("OnPropertiesGet, the serviceId is {}", serviceId);

                // 读取本地的属性值并上报
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setServiceId(serviceId);

                // 上报本地的属性值
                bridgeDevice.getClient().respondPropsGet(deviceId, requestId,
                    Collections.singletonList(serviceProperty));
            }
        });
    }

    private static void uploadAndDownloadFile(BridgeDevice bridgeDevice) throws InterruptedException {
        FileManagerService fileManagerService = bridgeDevice.getFileManagerService();
        fileManagerService.setBridgeFileMangerListener(new BridgeFileMangerListener() {
            @Override
            public void onUploadUrl(UrlResponse param, String deviceId) {
                log.info("the upload url is {}", param.getUrl());
                log.info("the upload bucketName is {}", param.getBucketName());
                log.info("the upload objectName  is {}", param.getObjectName());
                log.info("the upload expire is {}", param.getExpire());
                log.info("the fileAttributes is {}", param.getFileAttributes());

                // 收到文件上传的URL后，用户实现向URL上传文件

                // 上报文件上传结果
                OpFileStatusRequest opFileStatusRequest = new OpFileStatusRequest();
                opFileStatusRequest.setObjectName("objectName");
                opFileStatusRequest.setResultCode(RESULT_CODE_OF_FILE_UP);
                opFileStatusRequest.setStatusCode(200);
                opFileStatusRequest.setStatusDescription("upload file success");
                fileManagerService.reportUploadFileStatus(opFileStatusRequest, null);
            }

            @Override
            public void onDownloadUrl(UrlResponse param, String deviceId) {
                if (Objects.isNull(param)) {
                    log.error("the response of url is null");
                }

                log.info("the download url is {}", param.getUrl());
                log.info("the download bucketName is {}", param.getBucketName());
                log.info("the download objectName  is {}", param.getObjectName());
                log.info("the download expire is {}", param.getExpire());
                log.info("the download fileAttributes is {}", param.getFileAttributes());

                // 收到文件上传的URL后，从URL下载文件

                // 上报文件下载结果
                OpFileStatusRequest opFileStatusRequest = new OpFileStatusRequest();
                opFileStatusRequest.setObjectName("objectName");
                opFileStatusRequest.setResultCode(0);
                opFileStatusRequest.setStatusCode(200);
                opFileStatusRequest.setStatusDescription("download file success");
                fileManagerService.reportUploadFileStatus(opFileStatusRequest, null);
            }
        });

        UrlRequest urlRequest = new UrlRequest();
        urlRequest.setFileName(FILE_NAME);
        Map<String, Object> map = new HashMap<>();
        map.put(BridgeSDKConstants.HASH_CODE, HASH_CODE);
        map.put(BridgeSDKConstants.SIZE, SIZE);
        urlRequest.setFileAttributes(map);
        fileManagerService.getUploadUrlOfBridge(DEVICE_ID, urlRequest, null);

        Thread.sleep(3000);
        fileManagerService.getDownloadUrlOfBridge(DEVICE_ID, urlRequest, null);
    }

    private static void disconnect(BridgeDevice bridgeDevice) {
        bridgeDevice.getClient().setBridgeDeviceDisConnListener(deviceId -> {
            // 打印断链的返回体
            log.info("the disconnected device is {}", deviceId);
        });
    }

    private static void logout(BridgeDevice bridgeDevice) {
        bridgeDevice.getClient().setLogoutListener((deviceId, requestId, map) -> {
            // 打印logout的返回体
            log.info("the requestId is {}", requestId);
            log.info("the deviceId is {}", deviceId);
            log.info("the response of login is {}", map.get("result_code"));
        });

        DefaultActionListenerImpl defaultLogoutActionListener = new DefaultActionListenerImpl("logout");
        bridgeDevice.getClient().logoutAsync(DEVICE_ID, REQ_ID, defaultLogoutActionListener);
    }

    private static void resetDeviceSecret(BridgeDevice bridgeDevice) {
        bridgeDevice.getClient().setResetDeviceSecretListener((deviceId, requestId, resultCode, newSecret) -> {
            // 打印重置密钥的返回体
            log.info("the requestId is {}", requestId);
            log.info("the deviceId is {}", deviceId);
            log.info("the resultCode is {}", resultCode);
        });
        bridgeDevice.getClient()
            .resetSecret(DEVICE_ID, REQ_ID, new DeviceSecret(DEVICE_SECRET, NEW_DEVICE_SECRET), null);
    }

    private static void handlerMessageDown(BridgeDevice bridgeDevice) {
        bridgeDevice.getClient().setBridgeDeviceMessageListener((deviceId, deviceMsg) -> {
            // 打印网桥消息下发的body体
            log.info("the deviceId is {}", deviceId);
            log.info("the message of device is {}", deviceMsg.toString());
        });
    }

    private static void handleCommand(BridgeDevice bridgeDevice) {
        bridgeDevice.getClient().setBridgeCommandListener((deviceId, requestId, bridgeCommand) -> {
            log.info("the requestId is {}", requestId);
            log.info("the deviceId is {}", deviceId);
            log.info("the command of device is {}", bridgeCommand);
            bridgeDevice.getClient().respondCommand(deviceId, requestId, new CommandRsp(0));
        });

    }

    private static void reportDeviceMessage(BridgeDevice bridgeDevice) {
        DeviceMessage deviceMessage = new DeviceMessage();
        deviceMessage.setName(MESSAGE_NAME);
        deviceMessage.setId(MESSAGE_ID);
        deviceMessage.setContent(MESSAGE_CONTENT);
        bridgeDevice.getClient().reportDeviceMessage(DEVICE_ID, deviceMessage, null);
    }

    private static void reportProperty(BridgeDevice bridgeDevice) {
        Map<String, Object> json = new HashMap<>();
        Random rand = new SecureRandom();
        json.put(PROPERTY, rand.nextFloat() * 100.0f);
        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setProperties(json);
        serviceProperty.setServiceId(SERVICE_ID);

        bridgeDevice.getClient()
            .reportProperties(DEVICE_ID, Collections.singletonList(serviceProperty),
                null);
    }
}
