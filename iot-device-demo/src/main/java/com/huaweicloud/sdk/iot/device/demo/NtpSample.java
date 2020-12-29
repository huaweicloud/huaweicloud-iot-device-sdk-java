package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.timesync.TimeSyncService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间同步样例
 */
public class NtpSample {
    private static final Logger log = LogManager.getLogger(NtpSample.class);

    public static void main(String[] args) {
        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String secret = "mysecret";

        //创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret);

        if (device.init() != 0) {
            return;

        }

        TimeSyncService timeSyncService = device.getTimeSyncService();
        timeSyncService.setListener((deviceSendTime, serverRecvTime, serverSendTime) -> {

            long now; //设备获取自己的当前时间戳，即从格林威治时间1970年01月01日00时00分00秒起至现在的毫秒数

            long deviceRecvTime = System.currentTimeMillis();
            now = (serverRecvTime + serverSendTime + deviceRecvTime - deviceSendTime) / 2;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info(sdf.format(new Date(now)));

            log.info(now);

        });

        timeSyncService.requestTimeSync();

    }

}
