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

package com.huaweicloud.sdk.iot.device.codegenerator.productparser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DeviceProfileParser {
    private static final Logger log = LogManager.getLogger(DeviceProfileParser.class);

    private static final String DEVICETYPE_CAPABILITY = "devicetype-capability";

    private static final String SERVICETYPE_CAPABILITY = "servicetype-capability";

    private static final String DEVICES_TITLE = "devices";

    private static final String SERVICES_TITLE = "services";

    private static final String INT = "int";

    private static final String LONG = "long";

    private static final String BOOLEAN = "boolean";

    private static final String DECIMAL = "decimal";

    private static final String DOUBLE = "double";

    private static final String STRING = "String";

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    private static final String RANDOM_INT = "random.nextInt(100)";

    private static final String RANDOM_LONG = "random.nextLong(100)";

    private static final String RANDOM_DOUBLE = "random.nextDouble()";

    private static final String STRING_HELLO = "\"hello\"";

    public static ProductInfo parseProductFile(String zipfile) {

        ProductInfo productInfo = new ProductInfo();

        try {

            // 读取设备能力及服务能力
            List<DeviceCapability> deviceCapabilities = null;
            Map<String, DeviceService> serviceCapabilityMap = new HashMap<String, DeviceService>();
            List<String> files = unZipFiles(zipfile, "tmp");

            for (String outpath : files) {
                if (outpath == null) {
                    continue;
                }
                // 读取设备能力
                if (outpath.contains(DEVICETYPE_CAPABILITY)) {
                    deviceCapabilities = getDeviceCapability(outpath);
                }
                // 读取服务能力
                if (outpath.contains(SERVICETYPE_CAPABILITY)) {
                    Map<String, DeviceService> serviceCapability = getServiceCapability(outpath);
                    if (serviceCapability != null) {
                        serviceCapabilityMap.putAll(serviceCapability);
                    }
                }
            }

            if (deviceCapabilities == null) {
                return productInfo;
            }

            productInfo.setDeviceCapability(deviceCapabilities.get(0));
            productInfo.setServiceCapabilityMap(serviceCapabilityMap);

        } catch (Exception e) {

            log.error("parse product file error " + e.getMessage());
        }

        return productInfo;
    }

    private static Map<String, DeviceService> getServiceCapability(String filePath) {
        if (filePath == null) {
            log.debug("service capability file path is null.");
            return null;
        }

        JsonFactory factory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper(factory);
        File from = new File(filePath);

        TypeReference<HashMap<String, List<DeviceService>>> typeRef
                = new TypeReference<HashMap<String, List<DeviceService>>>() {
        };
        HashMap<String, List<DeviceService>> hm;
        Map<String, DeviceService> serviceCapabilityMap = new HashMap<>();
        try {
            hm = objectMapper.readValue(from, typeRef);
            if (hm == null) {
                log.debug("hm is null, read service capability failed");
                return serviceCapabilityMap;
            }

            List<DeviceService> sevices = hm.get(SERVICES_TITLE);
            for (DeviceService serviceCapability : sevices) {

                for (ServiceProperty property : serviceCapability.getProperties()) {

                    // 创建实体属性一
                    // int|long|decimal|string|DateTime|jsonObject|enum|boolean|string
                    String dataType = property.getDataType();
                    if (dataType == null) {
                        continue;
                    }

                    if (dataType.equalsIgnoreCase(INT)) {
                        property.setJavaType(INT);
                        property.setVal(RANDOM_INT);
                    } else if (dataType.equalsIgnoreCase(LONG)) {
                        property.setJavaType(LONG);
                        property.setVal(RANDOM_LONG);
                    } else if (dataType.equalsIgnoreCase(BOOLEAN)) {
                        property.setJavaType(BOOLEAN);
                        property.setVal(TRUE);
                    } else if (dataType.equalsIgnoreCase(DECIMAL)) {
                        property.setJavaType(DOUBLE);
                        property.setVal(RANDOM_DOUBLE);
                    } else {
                        property.setJavaType(STRING);
                        property.setVal(STRING_HELLO);
                    }
                    if (property.getMethod() != null && property.getMethod().contains("W")) {
                        property.setWriteable(TRUE);
                    } else {
                        property.setWriteable(FALSE);
                    }
                }

                serviceCapabilityMap.put(serviceCapability.getServiceType(), serviceCapability);
            }
            return serviceCapabilityMap;
        } catch (Exception e) {
            log.error("getServiceCapability error " + e.getMessage());
        }
        return null;
    }

    private static List<DeviceCapability> getDeviceCapability(String filePath) {
        if (filePath == null) {
            log.debug("device capability path is null");
            return null;
        }

        JsonFactory factory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper(factory);

        File from = new File(filePath);
        TypeReference<HashMap<String, List<DeviceCapability>>> typeRef
                = new TypeReference<HashMap<String, List<DeviceCapability>>>() {
        };
        HashMap<String, List<DeviceCapability>> hm;

        try {
            hm = objectMapper.readValue(from, typeRef);
            if (hm == null) {
                log.debug("hm is null, read device capability failed.");
                return null;
            }

            return hm.get(DEVICES_TITLE);
        } catch (Exception e) {
            log.error("get device capability path failed:{}", e.getMessage());
        }
        return null;
    }

    public static List<String> unZipFiles(String zipFile, String descDir) throws IOException {
        List<String> files = new ArrayList<>();
        if (zipFile == null) {
            log.error("the filename is null");
            return files;
        }

        String zipFileName = Paths.get(zipFile).getFileName().toString();
        int pos = zipFileName.lastIndexOf('.');
        String name;

        if (pos > 0) { // If '.' is not the first  character.
            name = zipFileName.substring(0, pos);
        } else {
            log.error("the filename is invalid");
            return files;
        }

        try (ZipFile zip = new ZipFile(zipFile, StandardCharsets.UTF_8)) {
            File extractionPath = Paths.get(descDir, name).toFile();
            if (!extractionPath.exists()) {
                boolean mkdirs = extractionPath.mkdirs();
                if (!mkdirs) {
                    log.error("make dir failed");
                }
            }

            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String zipEntryName = entry.getName();

                File file = Paths.get(descDir, name, zipEntryName).toFile();

                // 判断路径是否存在,不存在则创建文件路径
                File nearestDir = entry.isDirectory() ? file : file.getParentFile();
                if (!nearestDir.exists()) {
                    boolean mkdirs = nearestDir.mkdirs();
                    if (!mkdirs) {
                        log.error("make dir failed");
                    }
                }

                // 判断文件全路径是否为文件夹,如果是,上面已经创建,不需要解压
                if (entry.isDirectory()) {
                    continue;
                }

                try (InputStream in = zip.getInputStream(entry);
                     FileOutputStream out = FileUtils.openOutputStream(file)) {

                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }

                    files.add(file.toString());
                }
            }
            return files;
        }

    }

}
