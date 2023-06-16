package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.constants.Constants;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 演示如何直接使用DeviceClient进行消息透传
 */
public class MessageSample {

    private static final Logger log = LogManager.getLogger(MessageSample.class);

    public static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws InterruptedException, IOException {

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            "5e06bfee334dd4f33759f5b3_demo", "secret", tmpCAFile);

        // 默认使用国际加密通信，若要使用国密通信可setGmssl为true
        device.getClient().getClientConf().setGmssl(false);

        // 默认使用不校验时间戳，若要校验则需设置对应的参数选择杂凑算法
        device.getClient().getClientConf().setCheckStamp(Constants.CHECK_STAMP_SHA256_OFF);

        if (device.init() != 0) {
            return;
        }

        // 接收平台下行消息
        device.getClient().setDeviceMessageListener(
            deviceMessage -> log.info("the onDeviceMessage is {}", deviceMessage.toString()));

        while (true) {

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

            // 上报自定义topic消息，注意需要先在平台配置自定义topic
            String topic = "$oc/devices/" + device.getDeviceId() + "/user/wpy";
            device.getClient().publishRawMessage(new RawMessage(topic, "hello raw message "),
                new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {
                        log.info("publishRawMessage ok: ");
                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("publishRawMessage fail: " + var2);
                    }
                });

            Thread.sleep(5000);
        }
    }
}
