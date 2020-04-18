package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.gateway.SubDevicesPersistence;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 将子设备信息保存到json文件。用户可以自己实现SubDevicesPersistence接口来进行替换
 */
public class SubDevicesFilePersistence implements SubDevicesPersistence {

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private static final Logger log = Logger.getLogger(SubDevicesFilePersistence.class);
    private SubDevInfo subDevInfoCache;

    public SubDevicesFilePersistence() {

        String confFile = SubDevicesFilePersistence.class.getClassLoader().getResource("subdevices.json").getPath();
        File file = new File(confFile);
        String content = null;
        try {
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }
        this.subDevInfoCache = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);
        log.info("subDevInfo:" + subDevInfoCache.toString());
    }


    @Override
    public DeviceInfo getSubDevice(String nodeId) {

        readLock.lock();
        try {
            return subDevInfoCache.getSubdevices().get(nodeId);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int addSubDevices(SubDevicesInfo SubDevicesInfo) {

        writeLock.lock();
        try {
            if (SubDevicesInfo.getVersion() > 0 && SubDevicesInfo.getVersion() <= subDevInfoCache.getVersion()) {
                log.info("version too low: " + SubDevicesInfo.getVersion());
                return -1;
            }

            if (addSubDeviceToFile(SubDevicesInfo) != 0) {
                log.info("write file fail ");
                return -1;
            }

            if (subDevInfoCache.getSubdevices() == null) {
                subDevInfoCache.setSubdevices(new HashMap<>());
            }

            SubDevicesInfo.getDevices().forEach((dev) -> {
                subDevInfoCache.getSubdevices().put(dev.getNodeId(), dev);
                log.info("add subdev: " + dev.getNodeId());
            });
            subDevInfoCache.setVersion(SubDevicesInfo.getVersion());
            log.info("version update to "+subDevInfoCache.getVersion());

        } finally {
            writeLock.unlock();
        }
        return 0;
    }

    @Override
    public int deleteSubDevices(SubDevicesInfo subDevicesInfo) {

        if (subDevicesInfo.getVersion() > 0 && subDevicesInfo.getVersion() <= subDevInfoCache.getVersion()) {
            log.info("version too low: " + subDevicesInfo.getVersion());
            return -1;
        }

        if (subDevInfoCache.getSubdevices() == null) {
            return -1;
        }

        if (rmvSubDeviceToFile(subDevicesInfo) != 0) {
            log.info("remove from file fail ");
            return -1;
        }

        subDevicesInfo.getDevices().forEach((dev) -> {
            subDevInfoCache.getSubdevices().remove(dev.getNodeId());
            log.info("rmv subdev :" + dev.getNodeId());
        });

        subDevInfoCache.setVersion(subDevicesInfo.getVersion());
        log.info("local version update to "+subDevicesInfo.getVersion());

        return 0;
    }

    @Override
    public long getVersion() {
        return subDevInfoCache.getVersion();
    }


    private int addSubDeviceToFile(SubDevicesInfo subDevicesInfo) {
        String confFile = SubDevicesFilePersistence.class.getClassLoader().getResource("subdevices.json").getPath();
        File file = new File(confFile);

        String content;
        try {
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return -1;
        }

        SubDevInfo subDevInfo = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);

        if (subDevInfo.getSubdevices() == null) {
            subDevInfo.setSubdevices(new HashMap<>());
        }
        subDevicesInfo.getDevices().forEach((dev) ->
                subDevInfo.getSubdevices().put(dev.getNodeId(), dev));
        subDevInfo.setVersion(subDevicesInfo.getVersion());

        try {
            FileUtils.writeStringToFile(file, JsonUtil.convertObject2String(subDevInfo), "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return -1;
        }
        return 0;
    }

    private int rmvSubDeviceToFile(SubDevicesInfo subDevicesInfo) {
        String confFile = SubDevicesFilePersistence.class.getClassLoader().getResource("subdevices.json").getPath();
        File file = new File(confFile);

        String content;
        try {
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return -1;
        }

        SubDevInfo subDevInfo = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);

        if (subDevInfo.getSubdevices() == null) {
            return 0;
        }

        subDevicesInfo.getDevices().forEach((dev) ->
                subDevInfo.getSubdevices().remove(dev.getNodeId()));
        subDevInfo.setVersion(subDevicesInfo.getVersion());

        try {
            FileUtils.writeStringToFile(file, JsonUtil.convertObject2String(subDevInfo), "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return -1;
        }
        return 0;
    }

}
