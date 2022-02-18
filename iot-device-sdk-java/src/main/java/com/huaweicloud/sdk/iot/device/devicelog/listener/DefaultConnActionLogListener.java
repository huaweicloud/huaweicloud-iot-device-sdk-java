package com.huaweicloud.sdk.iot.device.devicelog.listener;

import com.huaweicloud.sdk.iot.device.devicelog.DeviceLogService;
import com.huaweicloud.sdk.iot.device.transport.ConnectActionListener;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.HashMap;
import java.util.Map;

public class DefaultConnActionLogListener implements ConnectActionListener {
    private DeviceLogService deviceLogService;

    public DefaultConnActionLogListener(DeviceLogService deviceLogService) {
        this.deviceLogService = deviceLogService;
    }

    @Override
    public void onSuccess(IMqttToken iMqttToken) {

        deviceLogService.reportDeviceLog(String.valueOf(System.currentTimeMillis()), "DEVICE_STATUS",
            "connect success");
        if (deviceLogService.getConnectFailedMap() != null) {
            String timestamp = (String) deviceLogService.getConnectFailedMap().keySet().toArray()[0];
            deviceLogService.reportDeviceLog(timestamp, "DEVICE_STATUS",
                deviceLogService.getConnectFailedMap().get(timestamp));
        }
    }

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

        Map<String, String> map = new HashMap<>();
        map.put(String.valueOf(System.currentTimeMillis()), "connect failed, the reason is " + throwable.getMessage());
        deviceLogService.setConnectFailedMap(map);
    }
}
