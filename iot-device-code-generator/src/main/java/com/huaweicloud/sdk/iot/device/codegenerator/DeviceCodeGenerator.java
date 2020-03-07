package com.huaweicloud.sdk.iot.device.codegenerator;

import com.huaweicloud.sdk.iot.device.codegenerator.ProductParser.DeviceProfileParser;
import com.huaweicloud.sdk.iot.device.codegenerator.ProductParser.DeviceService;
import com.huaweicloud.sdk.iot.device.codegenerator.ProductParser.ProductInfo;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 此工具根据产品模型文件自动生成设备代码
 */
public class DeviceCodeGenerator {

    private static final Logger log = Logger.getLogger(DeviceCodeGenerator.class);


    public static void main(String[] args) throws IOException, TemplateException {


        if (args.length < 1) {
            System.out.println("input your product file path");
            return;
        }
        String productZipPath = args[0];

        //提取资源文件到当前目录
        extractResources();

        DeviceProfileParser.unZipFiles("tmp\\generated-demo.zip", "");

        ProductInfo productInfo = DeviceProfileParser.parseProductFile(productZipPath);
        generateService(productInfo);
        generateDevice(productInfo);

        log.info("demo code generated to: " + new File("").getAbsolutePath() + "\\generated-demo");

        //删除临时文件
        deleteFile(new File("tmp\\"));
    }


    private static void extractResources() throws IOException {
        new File("tmp\\").mkdir();
        //提取资源文件到当前目录
        try (InputStream inputStream = DeviceCodeGenerator.class.getClassLoader()
                .getResourceAsStream("generated-demo.zip");
             OutputStream outputStream = new FileOutputStream("tmp\\generated-demo.zip")) {
            IOUtils.copy(inputStream, outputStream);
        }
        try (InputStream inputStream = DeviceCodeGenerator.class.getClassLoader()
                .getResourceAsStream("device.ftl");
             OutputStream outputStream = new FileOutputStream("tmp\\device.ftl")) {
            IOUtils.copy(inputStream, outputStream);
        }
        try (InputStream inputStream = DeviceCodeGenerator.class.getClassLoader()
                .getResourceAsStream("service.ftl");
             OutputStream outputStream = new FileOutputStream("tmp\\service.ftl")) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    public static void generateDevice(ProductInfo productInfo) throws TemplateException, IOException {
        Configuration cfg = new Configuration();
        try {

            cfg.setDirectoryForTemplateLoading(new File("tmp\\"));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Template template = cfg.getTemplate("device.ftl");

            File file = new File("generated-demo/src/main/java/com/huaweicloud/sdk/iot/device/demo/DeviceMain.java");
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

            cfg.setDirectoryForTemplateLoading(new File("tmp\\"));
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


    public static void deleteFile(File dirFile) {

        if (dirFile == null) {
            return;
        }
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return;
        }

        if (dirFile.isFile()) {
            dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                DeviceCodeGenerator.deleteFile(file);
            }
        }

        dirFile.delete();
    }


}