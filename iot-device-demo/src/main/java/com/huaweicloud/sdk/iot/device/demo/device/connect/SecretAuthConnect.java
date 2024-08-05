package com.huaweicloud.sdk.iot.device.demo.device.connect;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.demo.device.MessageSample;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SecretAuthConnect {

    private static final Logger log = LogManager.getLogger(MessageSample.class);

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;


    public static IoTDevice secretTlsConnect(String url, String deviceId, String password) throws IOException {
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = SecretAuthConnect.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备, 用户请替换为自己的接入地址。
        IoTDevice device = new IoTDevice(url, deviceId, password, tmpCAFile);
        if (device.init() != 0) {
            return null;
        }
        return device;
    }
    public static void main(String[] args) throws InterruptedException, IOException {

        // 请将 xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com 替换为自己的接入地址
        String url = "ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "you device id";
        String password = "you password";

        // -------- 密钥认证 8883端口 有TLS(一种加密算法) -----
        IoTDevice deviceTls = secretTlsConnect(url, deviceId, password);
        if (deviceTls != null) {
            log.info("connect success url = {}, deviceId = {}", url, deviceId);
            deviceTls.getClient().close();
        }

        // -------- 密钥认证 1883端口 无TLS ---------
        // 请将 xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com 替换成对应的域名
        url = "tcp://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:1883";
        IoTDevice device = new IoTDevice(url, deviceId, password, null);
        if (device.init() != 0) {
            return;
        }

        // ------ 以下为消息上报下发示例 ----------
        // 接收平台下行消息
        device.getClient().setRawDeviceMessageListener(
                deviceMessage -> log.info("the UTF8-decoded message is {}", deviceMessage.toUTF8String()));
        int i = 0;
        while (i++ < 100) {
            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    log.info("reportDeviceMessage ok");
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.error("reportDeviceMessage fail: " + var2);
                }
            });
            Thread.sleep(5000);
        }
    }
}
