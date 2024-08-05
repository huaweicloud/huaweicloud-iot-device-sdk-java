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

package com.huaweicloud.sdk.iot.device.devicelog;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class DeviceLogService extends AbstractService {
    private static final String LOG_CONFIG = "log_config";

    private boolean logSwitch = false;

    private String endTime;

    private Map<String, String> connectLostMap;

    private Map<String, String> connectFailedMap;

    public Map<String, String> getConnectLostMap() {
        return connectLostMap;
    }

    public void setConnectLostMap(Map<String, String> connectLostMap) {
        this.connectLostMap = connectLostMap;
    }

    public Map<String, String> getConnectFailedMap() {
        return connectFailedMap;
    }

    public void setConnectFailedMap(Map<String, String> connectFailedMap) {
        this.connectFailedMap = connectFailedMap;
    }

    public boolean isLogSwitch() {
        return logSwitch;
    }

    public void setLogSwitch(boolean logSwitch) {
        this.logSwitch = logSwitch;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (LOG_CONFIG.equals(deviceEvent.getEventType())) {

            ObjectNode objectNode = JsonUtil.convertMap2Object(deviceEvent.getParas(), ObjectNode.class);

            String aSwitch = objectNode.get("switch").asText();
            String time = objectNode.get("end_time").asText();

            if ("on".equals(aSwitch)) {
                logSwitch = true;
            } else if ("off".equals(aSwitch)) {
                logSwitch = false;
            }

            setEndTime(time);
        }

    }

    /**
     * 设备上报日志内容
     *
     * @param timestamp 日志产生时间戳，精确到秒
     * @param type      日志类型，总共有如下几种：
     *                  DEVICE_STATUS ：设备状态
     *                  DEVICE_PROPERTY ：设备属性
     *                  DEVICE_MESSAGE ：设备消息
     *                  DEVICE_COMMAND：设备命令
     * @param content   日志内容
     */
    public void reportDeviceLog(String timestamp, String type, String content) {

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("type", type);
        map.put("content", content);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("log_report");
        deviceEvent.setServiceId("$log");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setParas(map);

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportEvent");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);

    }

    /**
     * 根据平台上设置的开关和结束时间来判断能否上报日志
     *
     * @return true：能上报日志  false：不具备上报的条件
     */
    public boolean canReportLog() {
        String time = this.getEndTime();
        if (time != null) {
            time = time.replace("T", "");
            time = time.replace("Z", "");
        }

        String timeStampFormat = "yyyyMMddHHmmss";
        SimpleDateFormat df = new SimpleDateFormat(timeStampFormat);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentTime = df.format(new Date(System.currentTimeMillis()));

        if (this.isLogSwitch() && (time == null || currentTime.compareTo(time) < 0)) {
            return true;
        }

        return false;
    }

}
