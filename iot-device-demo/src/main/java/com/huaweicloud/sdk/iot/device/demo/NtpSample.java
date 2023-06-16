package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.timesync.TimeSyncService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 时间同步样例
 */
public class NtpSample {

    private static final Logger log = LogManager.getLogger(NtpSample.class);

    public static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws IOException {
        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String secret = "mysecret";

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret, tmpCAFile);

        if (device.init() != 0) {
            return;

        }

        TimeSyncService timeSyncService = device.getTimeSyncService();
        timeSyncService.setListener((deviceSendTime, serverRecvTime, serverSendTime) -> {

            long now; // 设备获取自己的当前时间戳，即从格林威治时间1970年01月01日00时00分00秒起至现在的毫秒数

            long deviceRecvTime = System.currentTimeMillis();
            now = (serverRecvTime + serverSendTime + deviceRecvTime - deviceSendTime) / 2;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info(sdf.format(new Date(now)));

            log.info(now);

        });

        timeSyncService.requestTimeSync();

    }

}
