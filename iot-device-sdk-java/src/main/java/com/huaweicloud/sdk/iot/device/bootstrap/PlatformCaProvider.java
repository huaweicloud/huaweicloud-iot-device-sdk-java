package com.huaweicloud.sdk.iot.device.bootstrap;

import java.io.File;

/**
 * 平台CA证书提供者
 *
 * @since 1.1.3
 */
public interface PlatformCaProvider {
    /**
     * 用于获取验证服务端证书的CA文件
     *
     * @return 验证服务端证书的CA文件
     */
    File getIotCaFile();

    /**
     * 用于获取引导服务端证书的CA文件
     * <p>
     * 通常引导服务端与连接服务端证书通常由同一个CA签发，可使用同一个根CA证书文件。
     *
     * @return 验证引导端证书的CA文件
     */
    default File getBootstrapCaFile() {
        return getIotCaFile();
    }
}
