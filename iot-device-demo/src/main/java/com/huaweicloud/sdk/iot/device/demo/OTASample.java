package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.ota.OTAListener;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import okhttp3.*;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OTA sample，用来演示如何使用SDK的OTA能力
 */
public class OTASample {


    private static final Logger log = Logger.getLogger(OTASample.class);

    public static void downloadPackage(String url, String filePath, OTAService otaService, ActionListener listener) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                        0, null);
                listener.onFailure(null, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (call.isCanceled()) {
                    return;
                }

                if (!response.isSuccessful()) {
                    otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                            100, "download response fail");
                    listener.onFailure(response, new RuntimeException("response fail"));
                    return;
                }

                try (ResponseBody responseBody = response.body();
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(responseBody.byteStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath))) {

                    long total = responseBody.contentLength();
                    byte[] bytes = new byte[1024 * 8];
                    int len;
                    long current = 0;
                    while ((len = bufferedInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                        fileOutputStream.flush();
                        current += len;
                        //计算进度
                        int progress = (int) (80 * current / total);

                        otaService.reportOtaStatus(OTAService.OTA_CODE_SUCCESS, progress, null);
                    }

                    if (current == total) {
                        listener.onSuccess(null);
                    }

                } catch (Exception e) {
                    otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                            0, e.getMessage());
                    listener.onFailure(response, e);
                }
            }
        });
    }

    /**
     * 校验升级包
     */
    public static int checkPackage() {

        return 0;
    }


    /**
     * 安装升级包并重启
     */
    public static int installPackage() {
        return 0;
    }

    /**
     * 升级前检查
     *
     * @param otaPackage 升级包
     * @return 如果允许升级，返回0；返回非0表示不允许升级
     */
    public static int preCheck(OTAPackage otaPackage) {

        //对版本、剩余空间、剩余电量、信号质量等进行检查，如果不允许升级，上报OTAService中定义的错误码或者自定义错误码，返回-1

        //otaService.reportOtaStatus(OTAService.OTA_CODE_NO_NEED, 0, null);

        return 0;
    }


    public static void main(String args[]) throws Exception {

        //创建设备并初始化
        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret");
        if (device.init() != 0) {
            return;
        }

        OTAService otaService = new OTAService();

        //设备启动时上报版本号
        otaService.reportFwVersion("1.0");

        //OTA是一个service，需要add到设备,注意serviceId要填写ota_manager
        device.addService("ota_manager", otaService);
        otaService.setOtaListener(new OTAListener() {

            @Override
            public void onQueryVersion() {

                otaService.reportFwVersion("1.0");
            }

            @Override
            public void onNewPackage(OTAPackage otaPackage) {

                if (preCheck(otaPackage) != 0) {
                    log.error("preCheck failed");
                    return;
                }

                //升级包保存路径
                String filePath = "image.bin";

                downloadPackage(otaPackage.getUrl(), filePath, otaService, new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                        //校验下载的升级包
                        if (checkPackage() != 0) {
                            otaService.reportOtaStatus(OTAService.OTA_CODE_CHECK_FAIL, 0, null);
                            return;
                        }

                        //安装升级包
                        if (installPackage() != 0) {
                            otaService.reportOtaStatus(OTAService.OTA_CODE_INSTALL_FAIL, 0, null);
                            return;
                        }

                        //上报升级结果
                        otaService.reportOtaStatus(OTAService.OTA_CODE_SUCCESS, 100, null);

                        //上报升级后的版本号，平台需要根据版本号判断是否升级成功
                        otaService.reportFwVersion("1.0");
                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("download failed");
                    }
                });

            }
        });

        while (true) {
            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
        }

    }
}
