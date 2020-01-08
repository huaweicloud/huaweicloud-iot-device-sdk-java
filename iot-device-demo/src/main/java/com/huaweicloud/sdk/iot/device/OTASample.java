package com.huaweicloud.sdk.iot.device;

import com.huaweicloud.sdk.iot.device.ota.OTAListener;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

public class OTASample {


    private OkHttpClient okHttpClient;
    private String filePath;
    private IoTDevice iotDevice;
    private OTAPackage otaPackage;
    private OTAService otaService;

    public OTASample(IoTDevice iotDevice, String filePath) throws Exception {
        this.iotDevice = iotDevice;
        this.filePath = filePath;
        this.otaService = new OTAService();
        iotDevice.addService("ota_manager", otaService);
        otaService.setOtaListener(new OTAListener() {
            @Override
            public void onQueryVersion() {
                otaService.reportVersion("1.1", "1.1");
            }

            @Override
            public void onNewPackage(OTAPackage pkg) {
                OTASample.this.otaPackage = pkg;
                downloadPkg(pkg.getUrl());
            }
        });

        SSLContext sslContext = IotUtil.getSSLContext(iotDevice.getClient().getClientConf());

        okHttpClient = new OkHttpClient.Builder()
                .socketFactory(sslContext.getSocketFactory())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).build();
    }

//    public static void main(String agrs) throws Exception {
//
//        //创建设备并初始化
//        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
//                "5e06bfee334dd4f33759f5b3_demo", "mysecret");
//        if (device.init() != 0) {
//            return;
//        }
//
//        OTASample otaSample = new OTASample(device, "image.bin");
//
//    }

    private void downloadPkg(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
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
                            int progress = (int) (100 * current / total);

                            //下载包阶段最多90%进度
                            if (progress >= 90) {
                                progress = 90;
                            }
                            otaService.reportOtaStatus(OTAService.OTA_CODE_SUCCESS, progress, null);
                        }
                    } else {
                        otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                                100, "download response fail");
                    }
                } catch (Exception e) {
                    otaService.reportOtaStatus(OTAService.OTA_CODE_DOWNLOAD_TIMEOUT,
                            0, e.getMessage());
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

    private void checkPkg() {

    }

    private void installPkg() {

    }


}
