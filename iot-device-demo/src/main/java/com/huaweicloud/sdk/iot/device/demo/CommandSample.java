package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import org.apache.log4j.Logger;

import java.util.Map;


/**
 * 演示如何直接使用DeviceClient处理平台下发的命令
 */
public class CommandSample {

    private static Logger log = Logger.getLogger(CommandSample.class);


    public static void main(String[] args) throws InterruptedException {

        //创建设备并初始化
        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret");
        if (device.init() != 0) {
            return;
        }

        //设置监听器接收下行
        device.getClient().setCommandListener(new CommandListener() {
            @Override
            public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
                log.info("onCommand, serviceId = " + serviceId);
                log.info("onCommand , name = " + commandName);
                log.info("onCommand, paras =  " + paras.toString());

                //处理命令

                //发送命令响应
                device.getClient().respondCommand(requestId, new CommandRsp(0));
            }
        });

        log.info("waiting for command ...");


    }
}
