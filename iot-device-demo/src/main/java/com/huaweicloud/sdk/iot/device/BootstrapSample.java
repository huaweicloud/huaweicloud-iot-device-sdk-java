package com.huaweicloud.sdk.iot.device;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

/**
 * 演示设备启动时，通过引导服务获取真实的服务器地址
 */
public class BootstrapSample {

    public static void main(String args[]) {

        //配置引导客户端参数
        ClientConf clientConf = new ClientConf();
        clientConf.setBootstrapUri("ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883");
        clientConf.setDeviceId("5df08775334dd4f3373a44a3_demo");
        clientConf.setSecret("secret");

        //创建引导客户端，发起引导
        BootstrapClient bootstrapClient = new BootstrapClient(clientConf);
        bootstrapClient.bootstrap(new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                String address = (String) context;
                System.out.println("bootstrap success:" + address);

                //引导成功后关闭客户端
                bootstrapClient.close();

                //创建设备客户端参数
                ClientConf clientConf = new ClientConf();
                clientConf.setServerUri("ssl://" + address);//address的格式是地址+端口，前面要加上协议前缀
                clientConf.setDeviceId("5df08775334dd4f3373a44a3_demo");
                clientConf.setSecret("secret");

                IoTDevice device = new IoTDevice(clientConf);
                if (device.init() != 0) {
                    return;

                }
                device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
            }

            @Override
            public void onFailure(Object context, Throwable var2) {

                System.out.println("bootstrap failed:" + var2.getMessage());
            }
        });

    }
}
