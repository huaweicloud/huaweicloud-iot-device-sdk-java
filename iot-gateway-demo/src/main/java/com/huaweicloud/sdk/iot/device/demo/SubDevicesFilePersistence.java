/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.gateway.SubDevicesPersistence;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 将子设备信息保存到json文件。用户可以自己实现SubDevicesPersistence接口来进行替换
 */
public class SubDevicesFilePersistence implements SubDevicesPersistence {
    private static final Logger log = LogManager.getLogger(SubDevicesFilePersistence.class);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    private SubDevInfo subDevInfoCache;

    SubDevicesFilePersistence() {

        String confFile = SubDevicesFilePersistence.class.getClassLoader().getResource("subdevices.json").getPath();
        File file = new File(confFile);
        String content = null;
        try {
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }
        this.subDevInfoCache = JsonUtil.convertJsonStringToObject(content, SubDevInfo.class);
        log.info("subDevInfo is {}", subDevInfoCache.toString());
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
    public int addSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesInfo == null) {
            return -1;
        }

        writeLock.lock();
        try {
            if (subDevicesInfo.getVersion() > 0 && subDevicesInfo.getVersion() <= subDevInfoCache.getVersion()) {
                log.info("version too low, the version is {}", subDevicesInfo.getVersion());
                return -1;
            }

            if (addSubDeviceToFile(subDevicesInfo) != 0) {
                log.info("write file fail ");
                return -1;
            }

            if (subDevInfoCache.getSubdevices() == null) {
                subDevInfoCache.setSubdevices(new HashMap<>());
            }

            subDevicesInfo.getDevices().forEach((dev) -> {
                subDevInfoCache.getSubdevices().put(dev.getNodeId(), dev);
                log.info("add subdev, the nodeId is {}", dev.getNodeId());
            });
            subDevInfoCache.setVersion(subDevicesInfo.getVersion());
            log.info("version update to {}", subDevInfoCache.getVersion());

        } finally {
            writeLock.unlock();
        }
        return 0;
    }

    @Override
    public int deleteSubDevices(SubDevicesInfo subDevicesInfo) {

        if (subDevicesInfo.getVersion() > 0 && subDevicesInfo.getVersion() <= subDevInfoCache.getVersion()) {
            log.info("version too low, the version is {}", subDevicesInfo.getVersion());
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
            log.info("remove sub device, the nodeId is {}", dev.getNodeId());
        });

        subDevInfoCache.setVersion(subDevicesInfo.getVersion());
        log.info("local version update to {}", subDevicesInfo.getVersion());

        return 0;
    }

    @Override
    public long getVersion() {
        return subDevInfoCache.getVersion();
    }

    private int addSubDeviceToFile(SubDevicesInfo subDevicesInfo) {
        String addConfFile = SubDevicesFilePersistence.class.getClassLoader().getResource("subdevices.json").getPath();
        File file = new File(addConfFile);

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
        String rmvConfFile = SubDevicesFilePersistence.class.getClassLoader().getResource("subdevices.json").getPath();
        File file = new File(rmvConfFile);

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
