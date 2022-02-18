package com.huaweicloud.sdk.iot.device.codegenerator;

import com.huaweicloud.sdk.iot.device.codegenerator.productparser.DeviceProfileParser;
import com.huaweicloud.sdk.iot.device.codegenerator.productparser.DeviceService;
import com.huaweicloud.sdk.iot.device.codegenerator.productparser.ProductInfo;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 此工具根据产品模型文件自动生成设备代码
 */
public class DeviceCodeGenerator {

    private static final Logger log = LogManager.getLogger(DeviceCodeGenerator.class);

    private static final String CONTENT = "tmp";

    public static void main(String[] args) throws IOException, TemplateException {

        if (args.length < 1) {
            System.out.println("input your product file path");
            return;
        }
        String productZipPath = args[0];

        // 提取资源文件到当前目录
        extractResources();

        DeviceProfileParser.unZipFiles(CONTENT + File.separator + File.separator + "generated-demo.zip", "");

        ProductInfo productInfo = DeviceProfileParser.parseProductFile(productZipPath);
        generateService(productInfo);
        generateDevice(productInfo);

        log.info("demo code generated to: {}{}{}generated-demo", new File("").getCanonicalPath(), File.separator,
            File.separator);

        // 删除临时文件
        deleteFile(new File(CONTENT + File.separator + File.separator));

    }

    private static void extractResources() throws IOException {
        boolean mkdir = new File("tmp\\").mkdir();
        if (!mkdir) {
            return;
        }
        // 提取资源文件到当前目录
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

    private static void generateDevice(ProductInfo productInfo) throws TemplateException, IOException {
        Configuration cfg = new Configuration();
        String pathName = "generated-demo/src/main/java/com/huaweicloud/sdk/iot/device/demo/DeviceMain.java";

        try (Writer javaWriter = new FileWriter(new File(pathName))) {

            cfg.setDirectoryForTemplateLoading(new File("tmp\\"));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Template template = cfg.getTemplate("device.ftl");

            Map<String, Object> root = new HashMap<>();
            root.put("device", productInfo.getDeviceCapability());

            template.process(root, javaWriter);
            javaWriter.flush();

        } catch (IOException | TemplateException e) {
            log.error("generateDevice error " + e.getMessage());
            throw e;
        }
    }

    private static void generateService(ProductInfo productInfo) throws IOException, TemplateException {
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File("tmp\\"));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        Template template = cfg.getTemplate("service.ftl");

        try {

            for (String sid : productInfo.getServiceCapabilityMap().keySet()) {
                DeviceService deviceService = productInfo.getServiceCapabilityMap().get(sid);
                File file = FileUtils.getFile(
                    "generated-demo/src/main/java/com/huaweicloud/sdk/iot/device/demo/" + deviceService.getServiceType()
                        + "Service.java");

                Map<String, Object> root = new HashMap<String, Object>();
                root.put("service", deviceService);

                try (Writer javaWriter = new FileWriter(file)) {
                    template.process(root, javaWriter);
                    javaWriter.flush();
                    log.info("the file generation path is ：{} ", file.getCanonicalPath());
                }
            }

        } catch (IOException | TemplateException e) {
            log.error("generateService error" + e.getMessage());
            throw e;
        }
    }

    private static void deleteFile(File dirFile) {

        if (dirFile == null) {
            return;
        }
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return;
        }

        if (dirFile.isFile()) {
            boolean delete = dirFile.delete();
            if (!delete) {
                String name = dirFile.getName();
                log.error("delete file error, the file name is {}", name);
            }
        } else {

            for (File file : Objects.requireNonNull(dirFile.listFiles())) {
                DeviceCodeGenerator.deleteFile(file);
            }
        }

        boolean delete = dirFile.delete();
        if (!delete) {
            String name = dirFile.getName();
            log.error("delete file error, the file name is {}", name);
        }
    }

}