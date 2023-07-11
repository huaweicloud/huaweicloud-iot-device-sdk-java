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

package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.bootstrap.PlatformCaProvider;
import com.huaweicloud.sdk.iot.device.demo.ReportDeviceInfoSample;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class BaseBootstrapSample {
    /**
     * 设备发放设备侧引导地址（无需修改）
     */
    public static final String BOOTSTRAP_URI = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

    /**
     * 设备发放的设备侧CA证书，注意与IoTDA的设备侧区分开（此处指定包含iot所有平台的根CA的证书文件，如有特殊需求，请按需裁剪）
     */
    private static final String BOOTSTRAP_CA_RES_PATH = "ca.jks";

    private static final String BOOTSTRAP_CA_TMP_FILE_PATH = "huaweicloud-iotda-tmp-" + BOOTSTRAP_CA_RES_PATH;

    /**
     * IoT平台CA证书
     * <p/>
     * 注意：
     * <ul>
     *     <li> 此处指定包含iot所有平台的根CA的证书文件，如有特殊需求，请按需裁剪；
     *     <li> 由于历史原因，iot平台某些旧实例使用的证书不尽相同，详细说明请查看 <a href="https://support.huaweicloud.com/devg-iothub/iot_02_1004.html#section3">证书资源</a>。
     */
    private static final String IOT_ROOT_CA = "ca.jks";

    protected final static PlatformCaProvider PLATFORM_CA_PROVIDER = new PlatformCaProvider() {
        @Override
        public File getIotCaFile() {
            // 加载iot平台（设备发放）的ca证书，进行服务端校验
            File tmpCAFile = new File(BOOTSTRAP_CA_TMP_FILE_PATH);
            try (InputStream resource = ReportDeviceInfoSample.class.getClassLoader().getResourceAsStream(BOOTSTRAP_CA_RES_PATH)) {
                Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                throw new IllegalArgumentException("can't extract bootstrap ca, " + BOOTSTRAP_CA_RES_PATH);
            }
            return tmpCAFile;
        }

        @Override
        public File getBootstrapCaFile() {
            // 加载iot平台的ca证书，进行服务端校验
            URL resource = BootstrapClient.class.getClassLoader().getResource(IOT_ROOT_CA);
            if (Objects.isNull(resource)) {
                throw new IllegalArgumentException("iotda ca path is null, path=" + IOT_ROOT_CA);
            }

            return new File(resource.getPath());
        }
    };
}
