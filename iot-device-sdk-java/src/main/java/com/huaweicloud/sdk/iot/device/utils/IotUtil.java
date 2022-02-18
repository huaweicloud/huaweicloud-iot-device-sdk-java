package com.huaweicloud.sdk.iot.device.utils;

import com.huaweicloud.sdk.iot.device.client.ClientConf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

/**
 * IOT工具类
 */
public class IotUtil {
    private static final Logger log = LogManager.getLogger(IotUtil.class);

    private static final String TLS_VER = "TLSv1.2";

    private static AtomicLong requestId = new AtomicLong(0);

    /**
     * 从topic里解析出requestId
     *
     * @param topic topic
     * @return requestId
     */
    public static String getRequestId(String topic) {
        String[] tmp = topic.split("request_id=");
        return tmp[1];
    }

    /**
     * 从deviceid解析nodeId
     *
     * @param deviceId 设备id
     * @return 设备物理标识
     */
    public static String getNodeIdFromDeviceId(String deviceId) {

        try {
            return deviceId.substring(deviceId.indexOf("_") + 1);
        } catch (Exception e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return null;
        }

    }

    /**
     * 根据请求topic构造响应topic
     *
     * @param topic 请求topic
     * @return 响应topic
     */
    public static String makeRspTopic(String topic) {

        try {
            String[] tmp = topic.split("request_id");
            return tmp[0] + "response/" + "request_id" + tmp[1];
        } catch (Exception e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return null;
        }
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static String getTimeStamp() {

        String timeStampFormat = "yyyyMMdd'T'HHmmss'Z'";

        SimpleDateFormat df = new SimpleDateFormat(timeStampFormat);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(System.currentTimeMillis()));

    }

    /**
     * 生成requestId
     *
     * @return requestId
     */
    public static String generateRequestId() {

        return Long.toString(requestId.incrementAndGet());

    }

    /**
     * HmacSHA256
     *
     * @param str       输入字符串
     * @param timeStamp 时间戳
     * @return hash后的字符串
     */
    public static String sha256Mac(String str, String timeStamp) {
        String passWord = null;
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(timeStamp.getBytes("UTF-8"), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] bytes = sha256Hmac.doFinal(str.getBytes("UTF-8"));
            passWord = byteArrayToHexString(bytes);
        } catch (Exception e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }
        return passWord;
    }

    /**
     * bytes转十六进制字符串
     *
     * @param b bytes
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toLowerCase(Locale.CHINESE);
    }

    private static TrustManager[] getTrustManager(File iotCertFile) throws Exception {

        try (FileInputStream stream = new FileInputStream(iotCertFile)) {
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(stream, null);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            TrustManager[] tm = tmf.getTrustManagers();
            return tm;
        }
    }

    private static SSLContext getSSLContextWithKeystore(KeyStore keyStore, String keyPassword, File iotCertFile)
        throws Exception {
        SSLContext context = SSLContext.getInstance(TLS_VER);

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, keyPassword.toCharArray());
        context.init(managerFactory.getKeyManagers(), getTrustManager(iotCertFile), null);
        return context;
    }

    /**
     * 根据配置获取ssl上下文
     *
     * @param clientConf 客户端配置
     * @return ssl上下文
     * @throws Exception ssl相关异常
     */
    public static SSLContext getSSLContext(ClientConf clientConf) throws Exception {

        if (clientConf.getKeyStore() != null) {

            return getSSLContextWithKeystore(clientConf.getKeyStore(), clientConf.getKeyPassword(),
                clientConf.getFile());
        } else {
            SSLContext sslContext = SSLContext.getInstance(TLS_VER);
            sslContext.init(null, getTrustManager(clientConf.getFile()), new SecureRandom());
            return sslContext;
        }

    }

    public static byte[] compress(String string, String encoding) {
        if (null == string || null == encoding) {
            return new byte[0];
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(string.getBytes(encoding));
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("compress failed " + e.getMessage());
        }
        return new byte[0];
    }
}
