package com.huaweicloud.sdk.iot.device.devicelog.listener;

import com.huaweicloud.sdk.iot.device.devicelog.DeviceLogService;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;

import java.util.HashMap;
import java.util.Map;

public class DefaultConnLogListener implements ConnectListener {
    private DeviceLogService deviceLogService;

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
