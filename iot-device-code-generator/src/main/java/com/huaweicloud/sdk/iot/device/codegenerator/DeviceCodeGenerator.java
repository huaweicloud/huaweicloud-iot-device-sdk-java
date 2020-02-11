package com.huaweicloud.sdk.iot.device.codegenerator;


import com.huaweicloud.sdk.iot.device.codegenerator.ProductParser.DeviceProfileParser;
import com.huaweicloud.sdk.iot.device.codegenerator.ProductParser.DeviceService;
import com.huaweicloud.sdk.iot.device.codegenerator.ProductParser.ProductInfo;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 此工具根据产品模型文件自动生成设备代码
 */
public class DeviceCodeGenerator {

    private static final Logger log = Logger.getLogger(DeviceCodeGenerator.class);


    public static void main(String[] args) throws IOException, TemplateException {

        String productZipPath;

        //入参传入产品模型zip包全路径，不传入默认使用resources目录下的product.zip
        if (args.length >= 1) {
            productZipPath = args[0];
        } else {
            productZipPath = DeviceCodeGenerator.class.getClassLoader().getResource("product.zip").getPath();
        }

        String demozip = DeviceCodeGenerator.class.getClassLoader().getResource("generated-demo.zip").getPath();
        DeviceProfileParser.unZipFiles(demozip, "");
        ProductInfo productInfo = DeviceProfileParser.parseProductFile(productZipPath);
        generateService(productInfo);
        generateDevice(productInfo);
        log.info("demo code generated to: " + new File("").getAbsolutePath() + "\\generated-demo");
    }

    public static void generateDevice(ProductInfo productInfo) throws TemplateException, IOException {
        Configuration cfg = new Configuration();
        try {

            cfg.setDirectoryForTemplateLoading(new File(DeviceCodeGenerator.class.getClassLoader().getResource("").getPath()));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Template template = cfg.getTemplate("device.ftl");

            File file = new File("generated-demo/src/main/java/com/huaweicloud/sdk/iot/device/demo/" + productInfo.getDeviceCapability().getDeviceType() + ".java");
            Map<String, Object> root = new HashMap<>();
            root.put("device", productInfo.getDeviceCapability());
            Writer javaWriter = new FileWriter(file);
            template.process(root, javaWriter);
            javaWriter.flush();
            javaWriter.close();


        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (TemplateException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void generateService(ProductInfo productInfo) throws IOException, TemplateException {
        Configuration cfg = new Configuration();
        try {

            cfg.setDirectoryForTemplateLoading(new File(DeviceCodeGenerator.class.getClassLoader().getResource("").getPath()));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Template template = cfg.getTemplate("service.ftl");

            for (String sid : productInfo.getServiceCapabilityMap().keySet()) {
                DeviceService deviceService = productInfo.getServiceCapabilityMap().get(sid);
                File file = new File("generated-demo/src/main/java/com/huaweicloud/sdk/iot/device/demo/" + deviceService.getServiceType() + "Service.java");

                Map<String, Object> root = new HashMap<String, Object>();
                root.put("service", deviceService);

                Writer javaWriter = new FileWriter(file);
                template.process(root, javaWriter);
                javaWriter.flush();
                log.info("文件生成路径：" + file.getCanonicalPath());
                javaWriter.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (TemplateException e) {
            e.printStackTrace();
            throw e;
        }
    }




}