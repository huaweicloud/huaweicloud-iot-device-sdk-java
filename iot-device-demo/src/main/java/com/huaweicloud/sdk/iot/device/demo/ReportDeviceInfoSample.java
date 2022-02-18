package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;

import java.io.File;
import java.net.URL;

/**
 * 设备信息样例，建议在设备建链成功后调用。
 */
public class ReportDeviceInfoSample {
    public static void main(String[] args) {
        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "702b1038-a174-4a1d-969f-f67f8df43c4a";
        String secret = "mysecret";

        //加载iot平台的ca证书，进行服务端校验
        URL resource = ReportDeviceLogSample.class.getClassLoader().getResource("ca.jks");
        File file = new File(resource.getPath());

        //创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret, file);

        if (device.init() != 0) {
            return;

        }

        String swVersion = "v1.0";
        String fwVersion = "v1.0";
        device.getClient().reportDeviceInfo(swVersion, fwVersion, null);
    }
}
