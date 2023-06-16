package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 演示如何直接使用DeviceClient处理平台下发的命令
 */
public class CommandSample {

    private static final Logger log = LogManager.getLogger(CommandSample.class);

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws InterruptedException, IOException {

        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "5e06bfee334dd4f33759f5b3_demo";
        String secret = "mysecret";

        // 从命令行获取设备参数
        if (args.length >= 3) {
            serverUri = args[0];
            deviceId = args[1];
            secret = args[2];
        }

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret, tmpCAFile);

        // 设置监听器接收下行
        device.getClient().setCommandListener(new CommandListener() {
            @Override
            public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
                log.info("onCommand, serviceId = {}", serviceId);
                log.info("onCommand , name = {}", commandName);
                log.info("onCommand, paras =  {}", paras.toString());

                // 处理命令

                // 发送命令响应
                device.getClient().respondCommand(requestId, new CommandRsp(0));
            }
        });

        if (device.init() != 0) {
            return;
        }
        log.info("waiting for command ...");
    }
}
