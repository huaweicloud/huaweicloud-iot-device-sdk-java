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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DeviceProfileParser {
    private static final String DEVICETYPE_CAPABILITY = "devicetype-capability";

    private static final String SERVICETYPE_CAPABILITY = "servicetype-capability";

    private static final String DEVICES_TITLE = "devices";

    private static final String SERVICES_TITLE = "services";

    private static final Logger log = LogManager.getLogger(DeviceProfileParser.class);

    public static ProductInfo parseProductFile(String zipfile) {

        ProductInfo productInfo = new ProductInfo();

        try {

            // 读取设备能力及服务能力
            List<DeviceCapability> deviceCapabilities = null;
            Map<String, DeviceService> serviceCapabilityMap = new HashMap<String, DeviceService>();
            List<String> files = unZipFiles(zipfile, "tmp\\");

            if (files != null) {
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
                        Map<String, DeviceService> serviceCapability = getServiceCapability(outpath, false);
                        if (serviceCapability != null) {
                            serviceCapabilityMap.putAll(serviceCapability);
                        }
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

    private static Map<String, DeviceService> getServiceCapability(String filePath, boolean checkForDataType) {
        if (filePath == null) {
            log.debug("service capability file path is null.");
            return null;
        }

        JsonFactory factory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper(factory);
        File from = new File(filePath);

        TypeReference<HashMap<String, List<DeviceService>>> typeRef
            = new TypeReference<HashMap<String, List<DeviceService>>>() {};
        HashMap<String, List<DeviceService>> hm = null;
        Map<String, DeviceService> serviceCapabilityMap = new HashMap<String, DeviceService>();
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
                    //int|long|decimal|string|DateTime|jsonObject|enum|boolean|string
                    String dataType = property.getDataType();
                    if (dataType.equalsIgnoreCase("int")) {
                        property.setJavaType("int");
                        property.setVal("random.nextInt(100)");
                    } else if (dataType.equalsIgnoreCase("long")) {
                        property.setJavaType("long");
                        property.setVal("random.nextInt(100)");
                    } else if (dataType.equalsIgnoreCase("boolean")) {
                        property.setJavaType("boolean");
                        property.setVal("true");
                    } else if (dataType.equalsIgnoreCase("decimal")) {
                        property.setJavaType("double");
                        property.setVal("random.nextFloat()");
                    } else {
                        property.setJavaType("String");
                        property.setVal("\"hello\"");
                    }
                    if (property.getMethod() != null && property.getMethod().contains("W")) {
                        property.setWriteable("true");
                    } else {
                        property.setWriteable("false");
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
            = new TypeReference<HashMap<String, List<DeviceCapability>>>() {};
        HashMap<String, List<DeviceCapability>> hm;

        try {
            hm = objectMapper.readValue(from, typeRef);
            if (hm == null) {
                log.debug("hm is null, read device capability failed.");
                return null;
            }

            return hm.get(DEVICES_TITLE);
        } catch (Exception e) {

        }
        return null;
    }

    public static List<String> unZipFiles(String zipFile, String descDir) throws IOException {

        try (ZipFile zip = new ZipFile(zipFile, Charset.forName("UTF-8"))) {
            String name = zip.getName().substring(zip.getName().lastIndexOf('\\') + 1, zip.getName().lastIndexOf('.'));

            List<String> files = new ArrayList<>();

            File pathFile = new File(descDir + name);
            if (!pathFile.exists()) {
                boolean mkdirs = pathFile.mkdirs();
                if (!mkdirs) {
                    log.error("make dir failed");
                }
            }

            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                String zipEntryName = entry.getName();

                String outPath = (descDir + name + "/" + zipEntryName).replaceAll("\\*", "/");

                // 判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    boolean mkdirs = file.mkdirs();
                    if (!mkdirs) {
                        log.error("make dir failed");
                    }
                }
                // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }

                try (InputStream in = zip.getInputStream(entry);

                    FileOutputStream out = FileUtils.openOutputStream(FileUtils.getFile(outPath))) {

                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }

                    files.add(outPath);
                }
            }
            return files;
        }

    }

}
