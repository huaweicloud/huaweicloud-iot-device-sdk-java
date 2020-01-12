package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.ota.OTAListener;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OTA sample，用来演示如何使用SDK的OTA能力
 */
public class OTASample {


    private OkHttpClient okHttpClient;
    private String filePath;
    private IoTDevice iotDevice;
    private OTAPackage otaPackage;
    private OTAService otaService;

    private Logger log = Logger.getLogger(OTASample.class);

    public OTASample(IoTDevice iotDevice, String filePath) throws Exception {
        this.iotDevice = iotDevice;
        this.filePath = filePath;
        this.otaService = new OTAService();

        //OTA是一个service，需要add到设备,注意serviceId要填写ota_manager
        iotDevice.addService("ota_manager", otaService);
        otaService.setOtaListener(new OTAListener() {

            @Override
            public void onQueryVersion() {

                otaService.reportVersion("1.1", "1.1");
            }

            @Override
            public void onNewPackage(OTAPackage otaPackage) {
                handleNewPackage(otaPackage);

            }
        });

        SSLContext sslContext = IotUtil.getSSLContext(iotDevice.getClient().getClientConf());

        okHttpClient = new OkHttpClient.Builder()
                .socketFactory(sslContext.getSocketFactory())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).build();
    }

    private void handleNewPackage(OTAPackage otaPackage) {

        this.otaPackage = otaPackage;

        if (preCheck(otaPackage) != 0){
            log.error("preCheck failed");
            return;
        }

        downloadPackage(otaPackage.getUrl(), new ActionListener() {
            @Override
            public void onSuccess(Object context) {

                if (checkPackage() != 0){
                    otaService.reportOtaStatus(OTAService.OTA_CODE_CHECK_FAIL, 0, null);
                    return;
                }

                installPackage();
                otaService.reportOtaStatus(OTAService.OTA_CODE_SUCCESS, 100, null);
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("download failed");
            }
        });
    }

    private void downloadPackage(String url, ActionListener listener) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
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
                ResponseBody responseBody = null;
                BufferedInputStream bufferedInputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    if (call.isCanceled()) {
                        return;
                    }

                    if (response.isSuccessful()) {
                        responseBody = response.body();
                        File file = new File(filePath);
                        long total = responseBody.contentLength();
                        bufferedInputStream = new BufferedInputStream(responseBody.byteStream());
                        fileOutputStream = new FileOutputStream(file);
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

                        if (current == total){
                            listener.onSuccess(null);
                        }
                    } else {
                        otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                                100, "download response fail");
                        listener.onFailure(response, new RuntimeException("response fail"));
                    }
                } catch (Exception e) {
                    otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                            0, e.getMessage());
                    listener.onFailure(response, e);
                } finally {
                    if (null != responseBody) {
                        responseBody.close();
                    }
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }
            }
        });
    }

    /**
     * 校验升级包
     */
    private int checkPackage() {

        return -1;
    }


    /**
     * 安装升级包，需要用户实现
     */
    private void installPackage() {

    }

    /**
     * 升级前检查
     * @param otaPackage 升级包
     * @return 如果允许升级，返回0；返回非0表示不允许升级
     */
    private int preCheck(OTAPackage otaPackage) {

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

        OTASample otaSample = new OTASample(device, "image.bin");

        while (true) {

            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);

            Thread.sleep(10000);
        }
    }
}
