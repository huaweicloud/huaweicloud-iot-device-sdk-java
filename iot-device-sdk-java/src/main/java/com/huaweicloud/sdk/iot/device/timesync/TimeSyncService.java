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

package com.huaweicloud.sdk.iot.device.timesync;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间同步服务，提供简单的时间同步服务，使用方法：
 * IoTDevice device = new IoTDevice(...
 * TimeSyncService timeSyncService = device.getTimeSyncService();
 * timeSyncService.setListener(new TimeSyncListener() {
 *
 * @Override public void onTimeSyncResponse(long device_send_time, long server_recv_time, long server_send_time) {
 * long device_recv_time = System.currentTimeMillis();
 * long now = (server_recv_time + server_send_time + device_recv_time - device_send_time) / 2;
 * System.out.println("now is "+ new Date(now) );
 * }
 * });
 * timeSyncService.RequestTimeSync()
 */

public class TimeSyncService extends AbstractService {
    private TimeSyncListener listener;

    public TimeSyncListener getListener() {
        return listener;
    }

    /**
     * 设置时间同步响应监听器
     *
     * @param listener 监听器
     */
    public void setListener(TimeSyncListener listener) {
        this.listener = listener;
    }

    /**
     * 发起时间同步请求，使用TimeSyncListener接收响应
     */
    public void requestTimeSync() {
        Map<String, Object> node = new HashMap<>();
        node.put("device_send_time", System.currentTimeMillis());

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("time_sync_request");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$time_sync");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        DefaultActionListenerImpl defaultActionListener = new DefaultActionListenerImpl("reportEvent");

        getIotDevice().getClient().reportEvent(deviceEvent, defaultActionListener);
    }

    @Override
    public void onEvent(DeviceEvent deviceEvent) {
        if (listener == null) {
            return;
        }

        if (deviceEvent.getEventType().equalsIgnoreCase("time_sync_response")) {
            ObjectNode node = JsonUtil.convertMap2Object(deviceEvent.getParas(), ObjectNode.class);
            long deviceSendTime = node.get("device_send_time").asLong();
            long serverRecvTime = node.get("server_recv_time").asLong();
            long serverSendTime = node.get("server_send_time").asLong();

            listener.onTimeSyncResponse(deviceSendTime, serverRecvTime, serverSendTime);
        }
    }

}
