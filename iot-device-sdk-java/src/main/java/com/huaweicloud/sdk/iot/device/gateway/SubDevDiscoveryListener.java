package com.huaweicloud.sdk.iot.device.gateway;

import com.huaweicloud.sdk.iot.device.gateway.requests.ScanSubdeviceNotify;

public interface SubDevDiscoveryListener {
    /**
     * 平台通知网关扫描子设备
     *
     * @param scanSubdeviceNotify 子设备扫描通知
     * @return 0表示处理成功，其他表示处理失败
     */
    int onScan(ScanSubdeviceNotify scanSubdeviceNotify);
}
