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

package com.huaweicloud.sdk.iot.device.devicelog.listener;

import com.huaweicloud.sdk.iot.device.devicelog.DeviceLogService;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;

import java.util.HashMap;
import java.util.Map;

public class DefaultConnLogListener implements ConnectListener {
    private final DeviceLogService deviceLogService;

    public DefaultConnLogListener(DeviceLogService deviceLogService) {
        this.deviceLogService = deviceLogService;
    }

    @Override
    public void connectionLost(Throwable cause) {

        Map<String, String> map = new HashMap<>();
        map.put(String.valueOf(System.currentTimeMillis()), "connect lost");

        deviceLogService.setConnectLostMap(map);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        deviceLogService.reportDeviceLog(String.valueOf(System.currentTimeMillis()), "DEVICE_STATUS",
            "connect complete, the url is " + serverURI);

        if (deviceLogService.getConnectLostMap() != null) {
            String timestamp = (String) deviceLogService.getConnectLostMap().keySet().toArray()[0];
            deviceLogService.reportDeviceLog(timestamp, "DEVICE_STATUS",
                deviceLogService.getConnectLostMap().get(timestamp));
        }
    }
}
