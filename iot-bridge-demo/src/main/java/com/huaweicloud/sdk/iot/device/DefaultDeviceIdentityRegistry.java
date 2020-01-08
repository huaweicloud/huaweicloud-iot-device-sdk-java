package com.huaweicloud.sdk.iot.device;


import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * 默认的设备标识管理器，从文件中读取设备标识信息。用于可以自定义DeviceIdentityRegistry类进行替换
 */
public class DefaultDeviceIdentityRegistry implements DeviceIdentityRegistry {

    private Map<String, Map<String, String>> deviceIdentityMap;
    private Logger log = Logger.getLogger(this.getClass());

    public DefaultDeviceIdentityRegistry() {

        String confFile;
        confFile = DefaultDeviceIdentityRegistry.class.getClassLoader().getResource("deviceIdentity.json").getPath();
        File file = new File(confFile);
        String content = null;
        try {
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }
        deviceIdentityMap = JsonUtil.convertJsonStringToObject(content, Map.class);

    }

    @Override
    public DeviceIdentity getDeviceIdentity(String nodeId) {

        Map<String, String> map = deviceIdentityMap.get(nodeId);

        String json = JsonUtil.convertObject2String(map);

        return JsonUtil.convertJsonStringToObject(json, DeviceIdentity.class);
    }
}
