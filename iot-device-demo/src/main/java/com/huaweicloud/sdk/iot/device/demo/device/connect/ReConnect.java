package com.huaweicloud.sdk.iot.device.demo.device.connect;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.CustomOptions;
import com.huaweicloud.sdk.iot.device.client.handler.CustomBackoffHandler;
import com.huaweicloud.sdk.iot.device.demo.device.MessageSample;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ReConnect {

    private static final Logger log = LogManager.getLogger(MessageSample.class);

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    // 创建设备, 用户请替换为自己的接入地址。
    static IoTDevice device = new IoTDevice("tcp://xxxxxx:1883",
            "xxxxxxxxxx", "xxxxxxxxxx", null);

    private static int retryTimes = 0;
    public static void main(String[] args) throws InterruptedException, IOException {

        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = ReConnect.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 不使用使用默认断线重连
        CustomOptions customOptionsnew = new CustomOptions();
        customOptionsnew.setReConnect(false);
        customOptionsnew.setCustomBackoffHandler(new CustomBackoffHandler() {
            @Override
            public int backoffHandler(Connection connection) {
                int ret = -1;
                long time = 5000L;
                while (ret != 0) {
                    // 退避重连
                    if (retryTimes > 10) {
                        time = 30000L;
                    }
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        log.error("sleep failed, the reason is {}", e.getMessage());
                    }
                    retryTimes++;
                    ret = connection.connect();
                }
                retryTimes = 0;
                return 0;
            }
        });
        device.setCustomOptions(customOptionsnew);

        // 建链
        if (device.init() != 0) {
            return;
        }
    }
}


