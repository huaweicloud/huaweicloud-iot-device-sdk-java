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

package com.huaweicloud.sdk.iot.device.ota;

import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * OTA服务类，提供设备升级相关接口，使用方法：
 * IoTDevice device = new IoTDevice(...
 * OTAService otaService = device.getOtaService();
 * otaService.setOtaListener(new OTAListener() {
 * 具体参见OTASample
 */
public class OTAService extends AbstractService {

    // 升级上报的错误码，用户也可以扩展自己的错误码
    public static final int OTA_CODE_SUCCESS = 0; // 成功

    public static final int OTA_CODE_BUSY = 1; // 设备使用中

    public static final int OTA_CODE_SIGNAL_BAD = 2; // 信号质量差

    public static final int OTA_CODE_NO_NEED = 3; // 已经是最新版本

    public static final int OTA_CODE_LOW_POWER = 4; // 电量不足

    public static final int OTA_CODE_LOW_SPACE = 5; // 剩余空间不足

    public static final int OTA_CODE_DOWNLOAD_TIMEOUT = 6; // 下载超时

    public static final int OTA_CODE_CHECK_FAIL = 7; // 升级包校验失败

    public static final int OTA_CODE_UNKNOWN_TYPE = 8; // 升级包类型不支持

    public static final int OTA_CODE_LOW_MEMORY = 9; // 内存不足

    public static final int OTA_CODE_INSTALL_FAIL = 10; // 安装升级包失败

    public static final int OTA_CODE_INNER_ERROR = 255; // 内部异常

    private static final Logger log = LogManager.getLogger(OTAService.class);

    private OTAListener otaListener;

    private ExecutorService executorService; // OTA单独起一个线程处理

    /**
     * 设置OTA监听器
     *
     * @param otaListener OTA监听器
     */
    public void setOtaListener(OTAListener otaListener) {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        this.otaListener = otaListener;
    }

    /**
     * 上报升级状态
     *
     * @param result      升级结果
     * @param progress    升级进度0-100
     * @param version     当前版本
     * @param description 具体失败的原因，可选参数
     */
    public void reportOtaStatus(int result, int progress, String version, String description) {

        Map<String, Object> node = new HashMap<>();
        node.put("result_code", result);
        node.put("progress", progress);
        if (description != null) {
            node.put("description", description);
        }
        node.put("version", version);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("upgrade_progress_report");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$ota");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportOtaStatus");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);
    }

    /**
     * 上报固件版本信息
     *
     * @param version 固件版本
     */
    public void reportVersion(String version) {

        Map<String, Object> node = new HashMap<>();
        node.put("fw_version", version);
        node.put("sw_version", version);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("version_report");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$ota");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportVersion");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);

    }

    /**
     * 接收OTA事件处理
     *
     * @param deviceEvent 服务事件
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        String result = "";
        Future<String> success = null;
        if (otaListener == null) {
            log.info("otaListener is null");
            return;
        }

        if (deviceEvent.getEventType().equalsIgnoreCase("version_query")) {
            OTAQueryInfo queryInfo = JsonUtil.convertMap2Object(deviceEvent.getParas(), OTAQueryInfo.class);
            otaListener.onQueryVersion(queryInfo);
            return;
        } else if (deviceEvent.getEventType().equalsIgnoreCase("firmware_upgrade")
            || deviceEvent.getEventType().equalsIgnoreCase("software_upgrade")) {

            OTAPackage pkg = JsonUtil.convertMap2Object(deviceEvent.getParas(), OTAPackage.class);

            success = executorService.submit(() -> otaListener.onNewPackage(pkg), "success");

        } else if (deviceEvent.getEventType().equalsIgnoreCase("firmware_upgrade_v2")
            || deviceEvent.getEventType().equalsIgnoreCase("software_upgrade_v2")) {

            OTAPackageV2 pkgV2 = JsonUtil.convertMap2Object(deviceEvent.getParas(), OTAPackageV2.class);

            success = executorService.submit(() -> otaListener.onNewPackageV2(pkgV2), "success");
        }
        try {
            if (success != null) {
                result = success.get();
            }
        } catch (Exception e) {
            log.error("get submit result failed " + e.getMessage());
        }

        if (result.equals("success")) {
            log.debug("submit task succeeded");
        }
    }

}
