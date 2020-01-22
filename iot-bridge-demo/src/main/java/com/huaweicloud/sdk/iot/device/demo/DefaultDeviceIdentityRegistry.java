package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 默认的设备标识管理器，从文件中读取设备标识信息。用于可以自定义DeviceIdentityRegistry类进行替换
 */
public class DefaultDeviceIdentityRegistry implements DeviceIdentityRegistry {

    private Map<String, Map<String, String>> deviceIdentityMap;
    private Logger log = Logger.getLogger(this.getClass());

    public DefaultDeviceIdentityRegistry() {


        String content = null;
        try (InputStream inputStream = DefaultDeviceIdentityRegistry.class.getClassLoader().getResourceAsStream("deviceIdentity.json");){
            content = readInputStream2String(inputStream);
        } catch (IOException e) {
            log.error(e);
        }
        deviceIdentityMap = JsonUtil.convertJsonStringToObject(content, Map.class);

    }

    @Override
    public DeviceIdentity getDeviceIdentity(String nodeId) {

        Map<String, String> map = deviceIdentityMap.get(nodeId);

        String json = JsonUtil.convertObject2String(map);

        return JsonUtil.convertJsonStringToObject(json, DeviceIdentity.class);
    }

    private String readInputStream2String(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");

    }
}
