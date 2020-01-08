package com.huaweicloud.sdk.iot.device.gateway;

import com.huaweicloud.sdk.iot.device.gateway.requests.ScanSubdeviceNotify;

public interface SubDevDiscoveryListener {


    /**
     * 平台通知网关扫描子设备
     *
     * @param scanSubdeviceNotify
     * @return
     */
    public abstract int onScan(ScanSubdeviceNotify scanSubdeviceNotify);
}
