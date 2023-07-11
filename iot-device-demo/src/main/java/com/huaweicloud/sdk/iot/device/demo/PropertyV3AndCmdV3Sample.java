/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.listener.CommandV3Listener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRspV3;
import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;
import com.huaweicloud.sdk.iot.device.client.requests.DevicePropertiesV3;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceData;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 演示如何使用旧接口（调用V3接口）数据上报/命令下发，一般用不到
 */
public class PropertyV3AndCmdV3Sample {

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    private static final Logger log = LogManager.getLogger(PropertyV3AndCmdV3Sample.class);

    public static void main(String[] args) throws IOException {

        String deviceId = "5e06bfee334dd4f33759f5b3_demo";
        String secret = "mysecret";

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备并初始化
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            deviceId, secret, tmpCAFile);
        if (device.init() != 0) {
            return;
        }

        DevicePropertiesV3 devicePropertiesV3 = new DevicePropertiesV3();
        devicePropertiesV3.setMsgType("deviceReq");

        ServiceData serviceData = new ServiceData();
        serviceData.setServiceId("analog");

        Map<String, Object> json1 = new HashMap<>();
        SecureRandom rand = new SecureRandom();

        json1.put("alarm", 1);
        json1.put("temperature", rand.nextFloat() * 100.0f);
        json1.put("humidity", rand.nextFloat() * 100.0f);
        json1.put("smokeConcentration", rand.nextFloat() * 100.0f);
        serviceData.setServiceData(json1);

        List<ServiceData> list = new ArrayList<>();
        list.add(serviceData);
        devicePropertiesV3.setServiceDatas(list);

        // 订阅V3 TOPIC
        device.getClient().subscribeTopic("/huawei/v1/devices/" + deviceId + "/command/json", new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("subscribe success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("subscribe failed");
            }
        }, 0);

        // 通过V3接口上报属性
        device.getClient().reportPropertiesV3(devicePropertiesV3, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("pubMessage success");
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportProperties failed" + var2.toString());
            }
        });

        // 设置监听器监听V3接口下发的命令
        device.getClient().setCommandV3Listener(new CommandV3Listener() {
            @Override
            public void onCommandV3(CommandV3 commandV3) {
                log.info("onCommand, msgType = {}", commandV3.getMsgType());
                log.info("onCommand, serviceId = {}", commandV3.getServiceId());
                log.info("onCommand, cmd = {}", commandV3.getCmd());
                log.info("onCommand, mid = {}", commandV3.getMid());
                log.info("onCommand, paras = {}", commandV3.getParas().toString());

                // 处理命令

                // 上报命令响应
                CommandRspV3 commandRspV3 = new CommandRspV3("deviceRsp", commandV3.getMid(), 0);
                Map<String, Object> json = new HashMap<>();
                json.put("result", 0);
                commandRspV3.setBody(json);

                device.getClient().responseCommandV3(commandRspV3, new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {
                        log.info("responseCommandV3 success");
                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("responseCommandV3 failed" + var2.toString());
                    }
                });
            }
        });

    }

}
