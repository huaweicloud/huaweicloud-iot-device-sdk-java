package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.ota.OTAListener;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
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
import javax.net.ssl.TrustManager;
import javax.xml.bind.DatatypeConverter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * OTA sample，用来演示如何实现设备升级。
 * 使用方法：用户在平台上创建升级任务后，修改main函数里设备参数后启动本例，即可看到设备收到升级通知，并下载升级包进行升级，
 * 并上报升级结果。在平台上可以看到升级结果
 */
public class OTASample implements OTAListener {

    private static final Logger log = LogManager.getLogger(OTASample.class);

    private IoTDevice device;

    private OTAService otaService;

    private OkHttpClient okHttpClient;

    private String version; //当前版本号

    private String packageSavePath; //升级包保存路径

    public OTASample(IoTDevice device, String packageSavePath) throws Exception {
        this.device = device;
        this.otaService = device.getOtaService();
        otaService.setOtaListener(this);
        this.packageSavePath = packageSavePath;
        this.version = "v1.0"; //修改为实际值

        this.okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .sslSocketFactory(createSSLSocketFactory()).hostnameVerifier((s, sslSession) -> {
                log.info("verify " + s);
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

            DefaultX509TrustManager defaultX509TrustManager = new DefaultX509TrustManager();
            sc.init(null, new TrustManager[] {defaultX509TrustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            log.error("create SSL Socket Factory error" + e.getMessage());
        }

        return ssfFactory;
    }

    private void downloadPackage(String url, String token, ActionListener listener) {

        Request request = new Request.Builder()
            .url(url).header("Authorization", "Bearer " + token)
            .build();
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
                    long current = 0;
                    while ((len = bufferedInputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                        fileOutputStream.flush();
                        current += len;

                        //计算进度
                        int progress = (int) (100 * current / total);
                        log.info("progress = " + progress);

                        //计算md5
                        digest.update(bytes, 0, len);

                    }

                    if (current == total) {
                        String md5 = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase(Locale.CHINA);
                        log.info("md5 = " + md5);

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
     * 校验升级包
     */
    public int checkPackage(OTAPackage otaPackage, String md5) {

        if (!md5.equalsIgnoreCase(otaPackage.getSign())) {
            log.error("md5 check fail");
            otaService.reportOtaStatus(OTAService.OTA_CODE_CHECK_FAIL, 0, version, "md5 check fail");
            return -1;
        }

        //TODO 增加其他校验
        return 0;
    }

    /**
     * 安装升级包，需要用户实现
     */
    public int installPackage() {

        //TODO
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
     * @param otaPackage 升级包
     * @return 如果允许升级，返回0；返回非0表示不允许升级
     */
    public int preCheck(OTAPackage otaPackage) {

        //todo 对版本号、剩余空间、剩余电量、信号质量等进行检查，如果不允许升级，上报OTAService中定义的错误码或者自定义错误码，返回-1

        /**
         * 上报升级状态
         * otaService.reportOtaStatus(OTAService.OTA_CODE_NO_NEED, 0, null);
         *
         */

        return 0;
    }

    @Override
    public void onQueryVersion() {
        otaService.reportVersion(version);
    }

    @Override
    public void onNewPackage(OTAPackage otaPackage) {

        log.info("otaPackage = " + otaPackage.toString());

        if (preCheck(otaPackage) != 0) {
            log.error("preCheck failed");
            return;
        }
        downloadPackage(otaPackage.getUrl(), otaPackage.getToken(), new ActionListener() {
            @Override
            public void onSuccess(Object context) {

                log.info("downloadPackage success");

                //校验下载的升级包
                if (checkPackage(otaPackage, (String) context) != 0) {
                    return;
                }

                //安装升级包
                if (installPackage() != 0) {
                    return;
                }

                //上报升级成功，注意版本号要携带更新后的版本号，否则平台会认为升级失败
                version = otaPackage.getVersion();
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

        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
            "deviceid", "secret");

        OTASample otaSample = new OTASample(device, "image.bin");
        otaSample.init();

        while (true) {
            device.getClient().reportDeviceMessage(new DeviceMessage("hello"), null);
            Thread.sleep(20000);
        }

    }
}
