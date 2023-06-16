package com.huaweicloud.sdk.iot.device.utils;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.constants.Constants;
import com.huaweicloud.sdk.iot.device.transport.Connection;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * IOT工具类
 */
public class IotUtil {
    private static final Logger log = LogManager.getLogger(IotUtil.class);

    private static final String TLS_VER = "TLSv1.2";

    private static final String GMTLS = "GMTLS";

    private static final String HMAC_SHA256 = "HmacSHA256";

    private static final String HMAC_SM3 = "HmacSM3";

    private static final long MIN_BACKOFF = 1000L;

    private static final long MAX_BACKOFF = 30 * 1000L; // 30 seconds

    private static final long DEFAULT_BACKOFF = 1000L;

    private static int retryTimes = 0;

    private static AtomicLong requestId = new AtomicLong(0);

    private static SecureRandom random = new SecureRandom();

    /**
     * 从topic里解析出requestId
     *
     * @param topic topic
     * @return requestId
     */
    public static String getRequestId(String topic) {
        if (topic == null || !topic.contains("request_id=")) {
            return null;
        }
        String[] tmp = topic.split("request_id=");
        return tmp[1];
    }

    /**
     * 从topic里解析出deviceId
     *
     * @param topic iotda的mqtt协议系统topic
     * @return deviceId
     */
    public static String getDeviceId(String topic) {
        if (topic == null || !topic.contains("/devices/")) {
            return null;
        }
        String[] split = topic.split("/devices/");
        return split[1].substring(0, split[1].indexOf("/"));
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
     * 退避重连
     *
     * @param connection
     * @return
     */
    public static int reConnect(Connection connection) {
        int ret = -1;
        while (ret != 0) {
            // 退避重连
            int lowBound = (int) (DEFAULT_BACKOFF * 0.8);
            int highBound = (int) (DEFAULT_BACKOFF * 1.0);
            long randomBackOff = random.nextInt(highBound - lowBound);
            int powParameter = retryTimes & 0x0F;
            long backOffWithJitter = (long) (Math.pow(2.0, (double) powParameter)) * (randomBackOff + lowBound);
            long waitTimeUntilNextRetry = Math.min(MIN_BACKOFF + backOffWithJitter, MAX_BACKOFF);
            try {
                Thread.sleep(waitTimeUntilNextRetry);
            } catch (InterruptedException e) {
                log.error("sleep failed, the reason is {}", e.getMessage());
            }
            retryTimes++;
            ret = connection.connect();
        }
        retryTimes = 0;
        return ret;
    }

    /**
     * HmacSHA256/HmacSM3
     *
     * @param str       输入字符串
     * @param timeStamp 时间戳
     * @param checkStamp 时间戳校验方法
     * @return hash后的字符串
     */
    public static String shaHMac(String str, String timeStamp, int checkStamp) {
        String passWord = null;
        try {
            String algorithm = checkStamp <= Constants.CHECK_STAMP_SHA256_ON ? HMAC_SHA256 : HMAC_SM3;
            Mac shaHmacMethod = Mac.getInstance(algorithm);
            SecretKeySpec secretKey = new SecretKeySpec(timeStamp.getBytes(StandardCharsets.UTF_8), algorithm);
            shaHmacMethod.init(secretKey);
            byte[] bytes = shaHmacMethod.doFinal(str.getBytes(StandardCharsets.UTF_8));
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

    private static X509Certificate loadX509CertificatePem(String crtFile) throws CertificateException, IOException {
        X509Certificate certificate;
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        try (InputStream inStream = new ByteArrayInputStream(crtFile.getBytes(StandardCharsets.UTF_8))) {
            certificate = (X509Certificate) cf.generateCertificate(inStream);
        }
        return certificate;
    }

    private static KeyStore getTrustKeyStore(Collection<Certificate> certs) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            for (Certificate cert : certs) {
                keyStore.setCertificateEntry("Huawei Cloud CA", cert);
            }
            log.info("load trust key store success");
            return keyStore;
        } catch (Exception e) {
            log.error("load key store error:", e);
        }
        return null;
    }

    private static TrustManager[] getTrustManager(File iotCertFile) throws Exception {
        if (iotCertFile == null) {
            return new TrustManager[] {new DefaultX509TrustManager()};
        }

        try (FileInputStream stream = new FileInputStream(iotCertFile)) {
            String filetype = FilenameUtils.getExtension(iotCertFile.getName());
            KeyStore ts = null;
            if ("jks".equals(filetype)) {
                ts = KeyStore.getInstance("JKS");
                ts.load(stream, null);
            } else {
                String certContent = IOUtils.toString(stream, StandardCharsets.UTF_8);
                Certificate cert = loadX509CertificatePem(certContent);
                ts = getTrustKeyStore(Collections.singleton(cert));
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);
            return tmf.getTrustManagers();
        }
    }

    /**
     * 根据配置获取ssl上下文
     *
     * @param clientConf 客户端配置
     * @return ssl上下文
     * @throws Exception ssl相关异常
     */
    public static SSLContext getSSLContext(ClientConf clientConf) throws Exception {

        String tlsVer = clientConf.isGmssl() ? GMTLS : TLS_VER;
        SSLContext sslContext = SSLContext.getInstance(tlsVer);
        if (clientConf.getKeyStore() != null) {

            KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            managerFactory.init(clientConf.getKeyStore(), clientConf.getKeyPassword().toCharArray());
            sslContext.init(managerFactory.getKeyManagers(),
                getTrustManager(clientConf.getFile()), SecureRandom.getInstanceStrong());
        } else {
            sslContext.init(null, getTrustManager(clientConf.getFile()),
                SecureRandom.getInstanceStrong());
        }
        return sslContext;
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

    public static class DefaultX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}
