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

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.demo.device.connect.DefaultX509TrustManager;
import com.huaweicloud.sdk.iot.device.ota.OTAListener;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
import com.huaweicloud.sdk.iot.device.ota.OTAPackageV2;
import com.huaweicloud.sdk.iot.device.ota.OTAQueryInfo;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.DatatypeConverter;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * OTAV2 sample，用来演示如何实现从obs传包以设备升级。
 * 使用方法：用户在平台上选择对应的obs桶和包后创建升级任务，修改main函数里设备参数后启动本例，即可看到设备收到升级通知，并下载升级包进行升级，
 * 并上报升级结果。在平台上可以看到升级结果
 */
public class OTAV2Sample implements OTAListener {

    private static final Logger log = LogManager.getLogger(OTASample.class);

    private final IoTDevice device;

    private final OTAService otaService;

    private final OkHttpClient okHttpClient;

    private String version; // 当前版本号

    private final String packageSavePath; // 升级包保存路径

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    private OTAV2Sample(IoTDevice device, String packageSavePath) throws Exception {
        this.device = device;
        this.otaService = device.getOtaService();
        otaService.setOtaListener(this);
        this.packageSavePath = packageSavePath;
        this.version = "v1.0"; // 修改为实际值

        System.out.println(System.getProperty("java.version"));

        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(), new DefaultX509TrustManager()).hostnameVerifier((s, sslSession) -> {
                    log.info("verify {}", s);
                    return true;
                })
                .build();

    }

    public int init() {
        return device.init();
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");

            sc.init(null, null, SecureRandom.getInstanceStrong());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            log.error("create SSL Socket Factory error" + e.getMessage());
        }

        return ssfFactory;
    }

    private void downloadPackageV2(String url, ActionListener listener) {

        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                log.error("onFailure " + e.toString());

                otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                        0, version, null);
                listener.onFailure(null, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                log.error("onResponse " + response.toString());

                if (call.isCanceled()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                            0, version, "download response fail");
                    listener.onFailure(response, new RuntimeException("response fail"));
                    return;
                }

                try (ResponseBody responseBody = response.body();
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(responseBody.byteStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(new File(packageSavePath))) {

                    MessageDigest digest = MessageDigest.getInstance("SHA-256");

                    long total = responseBody.contentLength();
                    byte[] bytes = new byte[1024 * 10];
                    int len;
                    long current = 0L;
                    while ((len = bufferedInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                        fileOutputStream.flush();
                        current += len;

                        // 计算进度
                        int progress = (int) (100 * current / total);
                        log.info("the progress is {}", progress);

                        // 计算md5
                        digest.update(bytes, 0, len);

                    }

                    if (current == total) {
                        String md5 = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase(Locale.CHINA);
                        log.info("the md5 is {}", md5);

                        otaService.reportOtaStatus(OTAService.OTA_CODE_SUCCESS, 100, version, null);
                        listener.onSuccess(md5);
                    }

                } catch (Exception e) {
                    otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                            0, version, e.getMessage());
                    listener.onFailure(response, e);
                }

            }
        });
    }

    /**
     * 安装升级包，需要用户实现
     */
    private int installPackage() {

        // TODO
        log.info("installPackage ok");

        /**
         * 如果安装失败，上报OTA_CODE_INSTALL_FAIL
         * otaService.reportOtaStatus(OTAService.OTA_CODE_INSTALL_FAIL, 0, version,null);
         *
         */

        return 0;
    }

    /**
     * 升级前检查，需要用户实现
     *
     * @param otaPackageV2 升级包
     * @return 如果允许升级，返回0；返回非0表示不允许升级
     */
    public int preCheck(OTAPackageV2 otaPackageV2) {

        // todo 对版本号、剩余空间、剩余电量、信号质量等进行检查，如果不允许升级，上报OTAService中定义的错误码或者自定义错误码，返回-1

        /**
         * 上报升级状态
         * otaService.reportOtaStatus(OTAService.OTA_CODE_NO_NEED, 0, null);
         *
         */

        return 0;
    }

    @Override
    public void onQueryVersion(OTAQueryInfo queryInfo) {
        log.info("queryInfo is {}", queryInfo);
        otaService.reportVersion(version);
    }

    @Override
    public void onNewPackage(OTAPackage pkg) {}

    @Override
    public void onNewPackageV2(OTAPackageV2 otaPackageV2) {

        log.info("otaPackageV2 is {}", otaPackageV2.toString());

        if (preCheck(otaPackageV2) != 0) {
            log.error("preCheck failed");
            return;
        }
        downloadPackageV2(otaPackageV2.getUrl(), new ActionListener() {
            @Override
            public void onSuccess(Object context) {

                log.info("downloadPackage success");

                // 安装升级包
                if (installPackage() != 0) {
                    return;
                }

                // 上报升级成功，注意版本号要携带更新后的版本号，否则平台会认为升级失败
                version = otaPackageV2.getVersion();
                otaService.reportOtaStatus(OTAService.OTA_CODE_SUCCESS, 100, version, "upgrade success");
                log.info("ota upgrade ok");

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("download failed");
            }
        });

    }

    public static void main(String[] args) throws Exception {

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 用户请替换为自己的接入地址。
        IoTDevice iotDevice = new IoTDevice("ssl://xxx.st1.iotda-device.cn-north-4.myhuaweicloud.com:8883",
                "deviceid", "secret", tmpCAFile);

        OTAV2Sample otaV2Sample = new OTAV2Sample(iotDevice, "image.bin");
        otaV2Sample.init();

        /**
         * 保持设备连接状态
         * while (true) {
         *     iotDevice.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
         *     Thread.sleep(20000);
         * }
         */
    }
}
