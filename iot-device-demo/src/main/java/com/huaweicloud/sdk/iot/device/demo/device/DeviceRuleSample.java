package com.huaweicloud.sdk.iot.device.demo.device;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 演示如何直接使用DeviceClient获取端侧规则并执行
 * 通过命令回调显示规则执行结果
 */
public class DeviceRuleSample {

    private static final Logger log = LogManager.getLogger(DeviceRuleSample.class);

    public static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws IOException, InterruptedException {
        // 用户请替换为自己的接入地址。
        String serverUri = "ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String password = "password";

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = DeviceRuleSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, password, tmpCAFile);
        device.getClient().setCommandListener(
            (requestId, serviceId, commandName, paras) -> {
                log.info("onCommand, serviceId = {}", serviceId);
                log.info("onCommand, name = {}", commandName);
                log.info("onCommand, paras =  {}", paras.toString());

                // 用户可以在该处进行命令处理

                // 发送命令响应
                device.getClient().respondCommand(requestId, new CommandRsp(0));
            });
        if (device.init() != 0) {
            return;
        }

        TimeUnit.SECONDS.sleep(5);
        log.info("begin to report device properties");
        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId("smokeDetector");
        Map<String, Object> properties = new HashMap<>();
        properties.put("temperature", 10);
        serviceProperty.setProperties(properties);
        device.getClient().reportProperties(Arrays.asList(serviceProperty), new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("pubMessage success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportProperties failed" + var2.toString());
            }
        });
    }
}
