package com.huaweicloud.sdk.iot.device.ota;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * OTA服务类，提供设备升级相关接口
 */
public class OTAService extends AbstractService {

    public static final int OTA_CODE_SUCCESS = 0;//成功
    public static final int OTA_CODE_INUSE = 1;  //设备使用中
    public static final int OTA_CODE_SIGNAL_BAD = 2;  //信号质量差
    public static final int OTA_CODE_NO_NEED = 3;  //已经是最新版本
    public static final int OTA_CODE_LOW_POWER = 4;  //电量不足
    public static final int OTA_CODE_LOW_SPACE = 5;  //剩余空间不足
    public static final int OTA_CODE_DOWNLOAD_TIMEOUT = 6;  //下载超时
    public static final int OTA_CODE_CHECK_FAIL = 7;  //升级包校验失败
    public static final int OTA_CODE_UNKNOWN_TYPE = 8;  //升级包类型不支持
    public static final int OTA_CODE_LOW_MEMORY = 9;  //内存不足
    public static final int OTA_CODE_INSTALL_FAIL = 10;  //安装升级包失败
    public static final int OTA_CODE_INNER_ERROR = 255;  // 内部异常
    private static final Logger log = Logger.getLogger(OTAService.class);
    private IoTDevice iotDevice;
    private OTAListener otaListener;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    /**
     * 设置OTA监听器
     *
     * @param otaListener OTA监听器
     */
    public void setOtaListener(OTAListener otaListener) {
        this.otaListener = otaListener;
    }


    /**
     * 上报升级状态
     *
     * @param result      升级结果
     * @param progress    升级进度0-100
     * @param description 具体失败的原因，可选参数
     */
    public void reportOtaStatus(int result, int progress, String description) {

        Map<String, Object> node = new HashMap<>();
        node.put("result_code", result);
        node.put("progress", progress);
        if (description != null) {
            node.put("description", description);
        }

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("upgrade_progress_report");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("ota");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        iotDevice.getClient().reportEvent(deviceEvent, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportOtaStatus failed: " + var2.getMessage());
            }
        });
    }


    /**
     * 上报版本信息
     *
     * @param fwVersion 固件版本
     * @param swVersion 软件版本
     */
    public void reportVersion(String fwVersion, String swVersion) {

        Map<String, Object> node = new HashMap<>();
        if (fwVersion != null) {
            node.put("fw_version", fwVersion);
        }
        if (swVersion != null) {
            node.put("sw_version", swVersion);
        }

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("version_report");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("ota");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        iotDevice.getClient().reportEvent(deviceEvent, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                log.error("reportVersion failed: " + var2.getMessage());
            }
        });

    }

    /**
     * 接收OTA事件处理
     *
     * @param deviceEvent 服务事件
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (otaListener == null) {
            log.info("otaListener is null");
            return;
        }

        if (deviceEvent.getEventType().equalsIgnoreCase("version_query")) {
            otaListener.onQueryVersion();
        } else if (deviceEvent.getEventType().equalsIgnoreCase("firmware_upgrade")) {

            OTAPackage pkg = JsonUtil.convertMap2Object(deviceEvent.getParas(), OTAPackage.class);

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    otaListener.onNewPackage(pkg);
                }
            });

        }
    }

}
