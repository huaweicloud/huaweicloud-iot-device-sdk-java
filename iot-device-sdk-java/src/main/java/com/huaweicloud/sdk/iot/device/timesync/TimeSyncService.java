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
