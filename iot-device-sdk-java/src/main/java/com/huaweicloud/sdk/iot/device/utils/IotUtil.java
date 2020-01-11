package com.huaweicloud.sdk.iot.device.utils;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import org.apache.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;


/**
 * IOT工具类
 */
public class IotUtil {


    private static final String TLS_VER = "TLSv1.2";
    private static Logger log = Logger.getLogger(IotUtil.class);
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

        String MSG_TIMESTAMP_FORMAT = "yyyyMMdd'T'HHmmss'Z'";

        SimpleDateFormat df = new SimpleDateFormat(MSG_TIMESTAMP_FORMAT);
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
    public static String sha256_mac(String str, String timeStamp) {
        String passWord = null;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(timeStamp.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(str.getBytes("UTF-8"));
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
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    private static TrustManager[] getTrustManager() throws Exception {

        try (InputStream stream = IotUtil.class.getClassLoader().getResourceAsStream("ca.jks") ) {
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(stream, null);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            TrustManager[] tm = tmf.getTrustManagers();
            return tm;
        }
    }

    private static SSLContext getSSLContextWithKeystore(KeyStore keyStore, String keyPassword) throws Exception {
        SSLContext context = SSLContext.getInstance(TLS_VER);

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, keyPassword.toCharArray());
        context.init(managerFactory.getKeyManagers(), getTrustManager(), null);
        return context;
    }

    /**
     * 根据配置获取ssl上下文
     * @param clientConf 客户端配置
     * @return ssl上下文
     * @throws Exception ssl相关异常
     */
    public static SSLContext getSSLContext(ClientConf clientConf) throws Exception {

        if (clientConf.getKeyStore() != null) {

            return getSSLContextWithKeystore(clientConf.getKeyStore(), clientConf.getKeyPassword());
        } else {
            SSLContext sslContext = SSLContext.getInstance(TLS_VER);
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext;
        }

    }


//    public static void main(String args[]) {
//
//
//    }


}
