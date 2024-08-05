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

package com.huaweicloud.sdk.iot.device.demo.device;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
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

/**
 * 演示如何直接使用DeviceClient进行消息透传
 */
public class MessageSample implements ConnectListener {
    private static final Logger log = LogManager.getLogger(MessageSample.class);

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    private final IoTDevice iotDevice;

    public MessageSample(IoTDevice device) {
        this.iotDevice = device;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = MessageSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 请将 xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com 替换为自己的接入地址。
        IoTDevice device = new IoTDevice("ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883", "your device id",
                "your device secret", tmpCAFile);
        device.getClient().setConnectListener(new MessageSample(device));

        // 默认使用不校验时间戳，若要校验则需设置对应的参数选择密钥加密算法
        device.getClient().getClientConf().setCheckStamp(Constants.CHECK_STAMP_SHA256_OFF);

        if (device.init() != 0) {
            return;
        }

        // 接收平台下行消息
        device.getClient().setRawDeviceMessageListener(
                deviceMessage -> log.info("the UTF8-decoded message is {}", deviceMessage.toUTF8String()));

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

            // 上报$oc开头的自定义topic消息，注意需要先在平台配置自定义topic
            String topicOc = "$oc/devices/" + device.getDeviceId() + "/user/wpy";
            device.getClient().publishRawMessage(new RawMessage(topicOc, "hello raw message "),
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
        }, 0);


        String topicOc = "$oc/devices/" + iotDevice.getDeviceId() + "/user/wpy";
        iotDevice.getClient().subscribeTopic(topic, new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                log.info("subscribe success topic = {}", topicOc);
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("subscribe failed topic = {}", topicOc);
            }
        }, 0);
    }
}
