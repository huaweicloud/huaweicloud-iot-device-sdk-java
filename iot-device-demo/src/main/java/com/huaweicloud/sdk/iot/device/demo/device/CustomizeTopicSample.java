package com.huaweicloud.sdk.iot.device.demo.device;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.constants.Constants;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 演示如何直接使用自定义Topic实现消息上下行
 */
public class CustomizeTopicSample implements ConnectListener {
    private static final Logger log = LogManager.getLogger(MessageSample.class);

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    private final IoTDevice iotDevice;

    public CustomizeTopicSample(IoTDevice iotDevice) {
        this.iotDevice = iotDevice;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CustomizeTopicSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 请将 xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com 替换为自己的接入地址。
        IoTDevice device = new IoTDevice("ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883", "your device id",
                "your device secret", tmpCAFile);
        device.getClient().setConnectListener(new CustomizeTopicSample(device));

        // 默认使用不校验时间戳，若要校验则需设置对应的参数选择密钥加密算法
        device.getClient().getClientConf().setCheckStamp(Constants.CHECK_STAMP_SHA256_OFF);

        if (device.init() != 0) {
            return;
        }

        // 接收平台下行消息
        device.getClient().setRawDeviceMessageListener(
                deviceMessage -> log.info("the UTF8-decoded message is {}", deviceMessage.toUTF8String()));

        int i = 3;
        while (i-- > 0) {
            // 上报自定义topic消息
            String topic = "hello/world";
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

            Thread.sleep(50000);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // 无需处理，默认底层有断线重连机制，若是需要自行编写重连机制，请看README
        log.error("connectionLost");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        // 订阅下行消息
        String topic = "hello/world";
        iotDevice.getClient().subscribeTopic(topic, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("subscribe success topic = {}", topic);
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("subscribe failed topic = {}", topic);
            }
        }, message -> {
            log.info("Received message = {} ", message);
            // 在这里处理接收到的消息
        }, 0);
    }
}
