package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.ota.OTAListener;
import com.huaweicloud.sdk.iot.device.ota.OTAPackage;
import com.huaweicloud.sdk.iot.device.ota.OTAPackageV2;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.service.DeviceCommand;
import com.huaweicloud.sdk.iot.device.service.Property;

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
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.DatatypeConverter;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 此例用来演示面向物模型编程的方法。用户只需要根据物模型定义自己的设备服务类，就可以直接对设备服务进行读写操作，SDK会自动
 * 的完成设备属性的同步和命令的调用。本例中实现的设备服务为烟感服务
 * 此例代码可以通过设备代码生成器（iot-device-code-generator工程）自动生成
 */


public class SmokeDetector {

    private static final String IOT_ROOT_CA_RES_PATH = "ca.jks";

    private static final String IOT_ROOT_CA_TMP_PATH = "huaweicloud-iotda-tmp-" + IOT_ROOT_CA_RES_PATH;

    public static void main(String[] args) throws IOException {
        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "5e06bfee334dd4f33759f5b3_demo";
        String secret = "secret";

        String packageSavePath = "./image.bin"; // 修改为目标ota存储地址
        String version = "v1.0"; // 测试用，不违反自己配置的版本号约束就行
        int reportInterval = 10000; //上报周期，毫秒

        // 从命令行获取设备参数
        if (args.length >= 3) {
            serverUri = args[0];
            deviceId = args[1];
            secret = args[2];
        }

        // 加载iot平台的ca证书，进行服务端校验
        File tmpCAFile = new File(IOT_ROOT_CA_TMP_PATH);
        try (InputStream resource = CommandSample.class.getClassLoader().getResourceAsStream(IOT_ROOT_CA_RES_PATH)) {
            assert resource != null;
            Files.copy(resource, tmpCAFile.toPath(), REPLACE_EXISTING);
        }

        // 创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret, tmpCAFile);

        // 创建设备服务
        SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
        device.addService("smokeDetector", smokeDetectorService);

        SampleOTAListener otaSample = new SampleOTAListener(device.getOtaService(), packageSavePath, version);
        device.getOtaService().setOtaListener(otaSample);
        if (device.init() != 0) {
            return;
        }

        // 启动自动周期上报
        smokeDetectorService.enableAutoReport(reportInterval);
    }

    /**
     * 烟感服务，支持属性：报警标志、烟雾浓度、温度、湿度
     * 支持的命令：响铃报警
     */
    public static class SmokeDetectorService extends AbstractService {

        // 按照设备模型定义属性，注意属性的name和类型需要和模型一致，writeable表示属性知否可写，name指定属性名
        @Property(name = "alarm", writeable = true)
        int smokeAlarm = 1;

        @Property(name = "smokeConcentration", writeable = false)
        float concentration = 0.0f;

        @Property(writeable = false)
        int humidity;

        @Property(writeable = false)
        float temperature;

        private final Logger log = LogManager.getLogger(this.getClass());

        // 定义命令，注意接口入参和返回值类型是固定的不能修改，否则会出现运行时错误
        @DeviceCommand(name = "ringAlarm")
        public CommandRsp alarm(Map<String, Object> paras) {
            int duration = (int) paras.get("duration");
            log.info("ringAlarm  duration is {}", duration);
            return new CommandRsp(0);
        }

        // setter和getter接口的命名应该符合java bean规范，sdk会自动调用这些接口
        public int getHumidity() {

            // 模拟从传感器读取数据
            humidity = new SecureRandom().nextInt(100);
            return humidity;
        }

        public void setHumidity(int humidity) {
            // humidity是只读的，不需要实现
        }

        public float getTemperature() {

            // 模拟从传感器读取数据
            temperature = new SecureRandom().nextInt(100);
            return temperature;
        }

        public void setTemperature(float temperature) {
            // 只读字段不需要实现set接口
        }

        public float getConcentration() {

            // 模拟从传感器读取数据
            concentration = new SecureRandom().nextFloat() * 100.0f;

            return concentration;
        }

        public void setConcentration(float concentration) {
            // 只读字段不需要实现set接口
        }

        public int getSmokeAlarm() {
            return smokeAlarm;
        }

        public void setSmokeAlarm(int smokeAlarm) {
            this.smokeAlarm = smokeAlarm;
            if (smokeAlarm == 0) {
                log.info("alarm is cleared by app");
            }
        }
    }


    public static class SampleOTAListener implements OTAListener {
        private static final Logger log = LogManager.getLogger(OTASample.class);


        private OTAService otaService;

        private OkHttpClient okHttpClient;

        private String version; // 当前版本号

        private String packageSavePath; // 升级包保存路径

        private SampleOTAListener(OTAService otaService, String packageSavePath, String version) {
            this.otaService = otaService;
            this.packageSavePath = packageSavePath;
            this.version = version;
            this.okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(600, TimeUnit.SECONDS)
                    .sslSocketFactory(createSSLSocketFactory(), new DefaultX509TrustManager()).hostnameVerifier((s, sslSession) -> {
                        log.info("verify {}", s);
                        return true;
                    })
                    .build();

        }


        private SSLSocketFactory createSSLSocketFactory() {
            SSLSocketFactory ssfFactory = null;
            try {

                SSLContext sc = SSLContext.getInstance("TLSv1.2");

                DefaultX509TrustManager defaultX509TrustManager = new DefaultX509TrustManager();
                sc.init(null, new TrustManager[]{defaultX509TrustManager}, SecureRandom.getInstanceStrong());
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
                public void onResponse(Call call, Response response) {

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
                         FileOutputStream fileOutputStream = new FileOutputStream(packageSavePath)) {

                        MessageDigest digest = MessageDigest.getInstance("SHA-256");

                        long total = responseBody.contentLength();
                        byte[] bytes = new byte[1024 * 10];
                        int len;
                        long current = 0;
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
         * 校验升级包
         */
        private int checkPackage(OTAPackage otaPackage, String md5) {

            if (!md5.equalsIgnoreCase(otaPackage.getSign())) {
                log.error("md5 check fail");
                otaService.reportOtaStatus(OTAService.OTA_CODE_CHECK_FAIL, 0, version, "md5 check fail");
                return -1;
            }

            // TODO 增加其他校验
            return 0;
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
         * @param otaPackage 升级包
         * @return 如果允许升级，返回0；返回非0表示不允许升级
         */
        public int preCheck(OTAPackage otaPackage) {

            // todo 对版本号、剩余空间、剩余电量、信号质量等进行检查，如果不允许升级，上报OTAService中定义的错误码或者自定义错误码，返回-1

            /**
             * 上报升级状态
             * otaService.reportOtaStatus(OTAService.OTA_CODE_NO_NEED, 0, null);
             *
             */

            return 0;
        }


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
        public void onQueryVersion() {
            otaService.reportVersion(version);
        }

        @Override
        public void onNewPackage(OTAPackage otaPackage) {

            log.info("otaPackage is {}", otaPackage.toString());

            if (preCheck(otaPackage) != 0) {
                log.error("preCheck failed");
                return;
            }
            downloadPackage(otaPackage.getUrl(), otaPackage.getToken(), new ActionListener() {
                @Override
                public void onSuccess(Object context) {

                    log.info("downloadPackage success");

                    // 校验下载的升级包
                    if (checkPackage(otaPackage, (String) context) != 0) {
                        return;
                    }

                    // 安装升级包
                    if (installPackage() != 0) {
                        return;
                    }

                    // 上报升级成功，注意版本号要携带更新后的版本号，否则平台会认为升级失败
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
                public void onResponse(Call call, Response response) {

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
                         FileOutputStream fileOutputStream = new FileOutputStream(packageSavePath)) {

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
    }

}
