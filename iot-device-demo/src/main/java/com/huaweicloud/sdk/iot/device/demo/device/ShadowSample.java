package com.huaweicloud.sdk.iot.device.demo.device;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.ShadowListener;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowData;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowRequest;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * 演示如何直接使用DeviceClient获取设备影子
 */
public class ShadowSample {
    private static final Logger log = LogManager.getLogger(ShadowSample.class);

    public static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws IOException {
        // 用户请替换为自己的接入地址。
        String serverUri = "ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String secret = "my secret";

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret, tmpCAFile);
        device.getClient().setShadowListener(new ShadowListener() {
            @Override
            public void onShadow(String requestId, List<ShadowData> shadowDataList) {
                log.info("requestId={}", requestId);
                log.info("shadowDataList={}", shadowDataList);
            }
        });

        if (device.init() != 0) {
            return;
        }

        ShadowRequest shadowRequest = new ShadowRequest();
        shadowRequest.setDeviceId(deviceId);
        shadowRequest.setServiceId("smokeDetector");
        device.getClient().getShadow(shadowRequest, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("getShadow success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("getShadow fail: " + var2);
            }
        });
    }
}
