package com.huaweicloud.sdk.iot.device.demo.bootstrap;

import com.huaweicloud.sdk.iot.device.bootstrap.BootstrapClient;
import com.huaweicloud.sdk.iot.device.bootstrap.PlatformCaProvider;

import java.io.File;
import java.net.URL;
import java.util.Objects;

public class BaseBootstrapSample {
    /**
     * 设备发放设备侧引导地址（无需修改）
     */
    public static final String BOOTSTRAP_URI = "ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883";

    /**
     * 设备发放的设备侧CA证书，注意与IoTDA的设备侧区分开（此处指定包含iot所有平台的根CA的证书文件，如有特殊需求，请按需裁剪）
     */
    private static final String BOOTSTRAP_CA_RES_PATH = "ca.jks";

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
            URL resource = BootstrapClient.class.getClassLoader().getResource(BOOTSTRAP_CA_RES_PATH);
            if (Objects.isNull(resource)) {
                throw new IllegalArgumentException("bootstrap ca path is null, path=" + BOOTSTRAP_CA_RES_PATH);
            }

            return new File(resource.getPath());
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
