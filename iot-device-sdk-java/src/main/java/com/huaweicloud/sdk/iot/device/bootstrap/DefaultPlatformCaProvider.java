package com.huaweicloud.sdk.iot.device.bootstrap;

import java.io.File;
import java.net.URL;
import java.util.Objects;

class DefaultPlatformCaProvider implements PlatformCaProvider {
    private final String iotPlatformCaFileResPath;

    public DefaultPlatformCaProvider(String iotPlatformCaFileResPath) {
        this.iotPlatformCaFileResPath = iotPlatformCaFileResPath;
    }

    @Override
    public File getIotCaFile() {
        URL resource = this.getClass().getClassLoader().getResource(iotPlatformCaFileResPath);
        if (Objects.isNull(resource)) {
            throw new IllegalArgumentException("iot platform ca path is null, path=" + iotPlatformCaFileResPath);
        }
        return new File(resource.getPath());
    }

    @Override
    public File getBootstrapCaFile() {
        return PlatformCaProvider.super.getBootstrapCaFile();
    }
}
