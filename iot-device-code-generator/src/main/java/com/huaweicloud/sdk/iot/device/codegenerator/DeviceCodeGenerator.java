package com.huaweicloud.sdk.iot.device.codegenerator;

import com.huaweicloud.sdk.iot.device.codegenerator.productparser.DeviceProfileParser;
import com.huaweicloud.sdk.iot.device.codegenerator.productparser.DeviceService;
import com.huaweicloud.sdk.iot.device.codegenerator.productparser.ProductInfo;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 此工具根据产品模型文件自动生成设备代码
 */
public class DeviceCodeGenerator {

    private static final Logger log = LogManager.getLogger(DeviceCodeGenerator.class);

    public static final String DEMO_TEMPLATE_NAME = "generated-demo";

    private static final String CONTENT = "tmp";

    public static final String DEMO_TEMPLATE_FILE_NAME = DEMO_TEMPLATE_NAME + ".zip";

    public static final String TARGET_MAIN_CLASS_PACKAGE_NAME = "src/main/java/com/huaweicloud/sdk/iot/device/demo/";

    public static final Path TARGET_MAIN_CLASS_DIR_PATH = Paths.get(DEMO_TEMPLATE_NAME, TARGET_MAIN_CLASS_PACKAGE_NAME);

    public static final String TARGET_MAIN_CLASS_FILE_NAME = "DeviceMain.java";

    public static final String MAIN_JAVA_TEMPLATE_FILE_NAME = "device.ftl";

    public static final String SERVICE_JAVA_TEMPLATE_FILE_NAME = "service.ftl";

    public static void main(String[] args) throws IOException, TemplateException {

        if (args.length < 1) {
            System.out.println("input your product file path");
            return;
        }

        String productZipPath = args[0];
        if (!(new File(productZipPath).exists())) {
            System.out.println("invalid file path");
            return;
        }

        // 提取资源文件到当前目录
        extractResources();
        DeviceProfileParser.unZipFiles(Paths.get(CONTENT, DEMO_TEMPLATE_FILE_NAME).toString(), "");

        ProductInfo productInfo = DeviceProfileParser.parseProductFile(productZipPath);
        generateService(productInfo);
        generateDevice(productInfo);

        log.info("demo code generated to: {}", Paths.get("", DEMO_TEMPLATE_NAME).toRealPath());

        // 删除临时文件
        deleteFile(new File(CONTENT));

    }

    private static void extractResources() throws IOException {
        boolean isMade = new File(CONTENT).mkdir();
        if (!isMade) {
            return;
        }

        // 提取资源文件到当前目录
        try (InputStream inputStream = DeviceCodeGenerator.class.getClassLoader()
                .getResourceAsStream(DEMO_TEMPLATE_FILE_NAME);
             OutputStream outputStream = new FileOutputStream(Paths.get(CONTENT, DEMO_TEMPLATE_FILE_NAME).toFile())) {
            assert inputStream != null;
            IOUtils.copy(inputStream, outputStream);
        }
        try (InputStream inputStream = DeviceCodeGenerator.class.getClassLoader()
                .getResourceAsStream(MAIN_JAVA_TEMPLATE_FILE_NAME);
             OutputStream outputStream = new FileOutputStream(Paths.get(CONTENT, MAIN_JAVA_TEMPLATE_FILE_NAME).toFile())) {
            assert inputStream != null;
            IOUtils.copy(inputStream, outputStream);
        }
        try (InputStream inputStream = DeviceCodeGenerator.class.getClassLoader()
                .getResourceAsStream(SERVICE_JAVA_TEMPLATE_FILE_NAME);
             OutputStream outputStream = new FileOutputStream(Paths.get(CONTENT, SERVICE_JAVA_TEMPLATE_FILE_NAME).toFile())) {
            assert inputStream != null;
            IOUtils.copy(inputStream, outputStream);
        }
    }

    private static void generateDevice(ProductInfo productInfo) throws TemplateException, IOException {
        Configuration cfg = new Configuration();
        File mainClassFile = Paths.get(TARGET_MAIN_CLASS_DIR_PATH.toString(), TARGET_MAIN_CLASS_FILE_NAME).toFile();
        try (Writer javaWriter = new FileWriter(mainClassFile)) {

            cfg.setDirectoryForTemplateLoading(new File(CONTENT));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Template template = cfg.getTemplate(MAIN_JAVA_TEMPLATE_FILE_NAME);

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
        cfg.setDirectoryForTemplateLoading(new File(CONTENT));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        Template template = cfg.getTemplate(SERVICE_JAVA_TEMPLATE_FILE_NAME);

        try {

            for (String sid : productInfo.getServiceCapabilityMap().keySet()) {
                DeviceService deviceService = productInfo.getServiceCapabilityMap().get(sid);
                File file = Paths.get(TARGET_MAIN_CLASS_DIR_PATH.toString(),
                        deviceService.getServiceType() + "Service.java").toFile();

                Map<String, Object> root = new HashMap<String, Object>();
                root.put("service", deviceService);

                try (Writer javaWriter = new FileWriter(file)) {
                    template.process(root, javaWriter);
                    javaWriter.flush();
                    log.info("the file generation path is :{} ", file.getCanonicalPath());
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

        if (dirFile.isDirectory()) {
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