package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.service.DeviceCommand;
import com.huaweicloud.sdk.iot.device.service.Property;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Random;


/**
 * 此例用来演示面向物模型编程的方法。用户只需要根据物模型定义自己的设备服务类，就可以直接对设备服务进行读写操作，SDK会自动
 * 的完成设备属性的同步和命令的调用。本例中实现的设备服务为烟感服务
 * 此例代码可以通过设备代码生成器（iot-device-code-generator工程）自动生成
 */

public class SmokeDetector {

    public static void main(String args[]) throws InterruptedException {

        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";
        String deviceId = "5e06bfee334dd4f33759f5b3_demo";
        String secret = "secret";

        //从命令行获取设备参数
        if (args.length >= 3) {
            serverUri = args[0];
            deviceId = args[1];
            secret = args[2];
        }

        //创建设备
        IoTDevice device = new IoTDevice(serverUri, deviceId, secret);

        //创建设备服务
        SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
        device.addService("smokeDetector", smokeDetectorService);

        if (device.init() != 0) {
            return;
        }

        //启动自动周期上报
        smokeDetectorService.enableAutoReport(10000);

    }

    /**
     * 烟感服务，支持属性：报警标志、烟雾浓度、温度、湿度
     * 支持的命令：响铃报警
     */
    public static class SmokeDetectorService extends AbstractService {

        //按照设备模型定义属性，注意属性的name和类型需要和模型一致，writeable表示属性知否可写，name指定属性名
        @Property(name = "alarm", writeable = true)
        int smokeAlarm = 1;

        @Property(name = "smokeConcentration", writeable = false)
        float concentration = 0.0f;

        @Property(writeable = false)
        int humidity;

        @Property(writeable = false)
        float temperature;

        private Logger log = Logger.getLogger(this.getClass());

        //定义命令，注意接口入参和返回值类型是固定的不能修改，否则会出现运行时错误
        @DeviceCommand(name = "ringAlarm")
        public CommandRsp alarm(Map<String, Object> paras) {
            int duration = (int) paras.get("duration");
            log.info("ringAlarm  duration = " + duration);
            return new CommandRsp(0);
        }

        //setter和getter接口的命名应该符合java bean规范，sdk会自动调用这些接口
        public int getHumidity() {

            //模拟从传感器读取数据
            humidity = new Random().nextInt(100);
            return humidity;
        }

        public void setHumidity(int humidity) {
            //humidity是只读的，不需要实现
        }

        public float getTemperature() {

            //模拟从传感器读取数据
            temperature = new Random().nextInt(100);
            return temperature;
        }

        public void setTemperature(float temperature) {
            //只读字段不需要实现set接口
        }

        public float getConcentration() {

            //模拟从传感器读取数据
            concentration = new Random().nextFloat()*100.0f;
            return concentration;
        }

        public void setConcentration(float concentration) {
            //只读字段不需要实现set接口
        }

        public int getSmokeAlarm() {
            return smokeAlarm;
        }

        public void setSmokeAlarm(int smokeAlarm) {

            this.smokeAlarm = smokeAlarm;
            if (smokeAlarm == 0){
                log.info("alarm is cleared by app");
            }
        }

    }
}
