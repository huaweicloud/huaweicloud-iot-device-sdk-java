# huaweicloud-iot-device-sdk-java

huaweicloud-iot-device-sdk-java提供设备接入华为云IoT物联网平台的Java版本的SDK，提供设备和平台之间通讯能力，以及设备服务、网关服务、OTA等高级服务，并且针对各种场景提供了丰富的demo代码。IoT设备开发者使用SDK可以大大简化开发复杂度，快速的接入平台。


* [支持特性](#支持特性)
* [如何使用](#如何使用)
* [设备初始化](#设备初始化)
* [上报消息](#上报消息)
* [上报设备属性](#上报设备属性)
* [处理平台下发的属性读写](#处理平台下发的属性读写)
* [处理平台下发的命令](#处理平台下发的命令)
* [面向物模型编程](#面向物模型编程)
* [使用设备代码生成器](#使用设备代码生成器)
* [使用证书认证](#使用证书认证)
* [如何贡献代码](#如何贡献代码)
* [API Documentation](https://cn-north-4-iot-sp.huaweicloud.com/assets/helpcenter/doc/index.html)

## 支持特性
- 支持设备消息、属性上报、属性读写、命令下发
- 支持网关服务、子设备管理、子设备消息转发
- 支持设备OTA服务
- 支持面向物模型编程
- 提供设备代码生成器根据产品模型自动生成设备代码
- 支持密码认证和证书认证两种设备认证方式
- 支持自定义topic

## 如何使用

依赖的版本：
* JDK ：1.8 +

因为huaweicloud-iot-device-sdk-java还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install 

项目中可以使用dependencyManagement引入依赖。

    <dependency>
                <groupId>com.huaweicloud.iot</groupId>
                <artifactId>iot-device-sdk-java</artifactId>
                <version>0.5.0</version>
    </dependency>
    
### 设备初始化

创建设备并初始化
```java
        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret");
        if (device.init() != 0) {
            return;
        }
```

### 上报消息

完整代码参见MessageSample.java
```java

       device.getClient().reportDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            log.info("reportDeviceMessage success: ");
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            log.error("reportDeviceMessage fail: "+var2);
        }
    });

```

### 上报设备属性
完整代码参见PropertySample.java
```java
     Map<String ,Object> json = new HashMap<>();
     Random rand = new Random();

     //按照物模型设置属性
     json.put("alarm", alarm);
     json.put("temperature", rand.nextFloat()*100.0f);
     json.put("humidity", rand.nextFloat()*100.0f);
     json.put("smokeConcentration", rand.nextFloat() * 100.0f);

     ServiceProperty serviceProperty = new ServiceProperty();
     serviceProperty.setProperties(json);
     serviceProperty.setServiceId("smokeDetector");//serviceId要和物模型一致

     device.getClient().reportProperties(Arrays.asList(serviceProperty), new ActionListener() {
         @Override
         public void onSuccess(Object context) {
             log.info("reportProperties success" );
         }

         @Override
         public void onFailure(Object context, Throwable var2) {
             log.error("reportProperties failed" + var2.toString());
         }   });

```

### 上报子设备属性
```java
     Map<String ,Object> json = new HashMap<>();
     Random rand = new Random();
     String subdeviceId = "xxxxx";

     //按照物模型设置属性
     json.put("alarm", alarm);
     json.put("temperature", rand.nextFloat()*100.0f);
     json.put("humidity", rand.nextFloat()*100.0f);
     json.put("smokeConcentration", rand.nextFloat() * 100.0f);

     ServiceProperty serviceProperty = new ServiceProperty();
     serviceProperty.setProperties(json);
     serviceProperty.setServiceId("smokeDetector");//serviceId要和物模型一致

     device.getClient().reportProperties(subdeviceId, Arrays.asList(serviceProperty), new ActionListener() {
         @Override
         public void onSuccess(Object context) {
             log.info("reportProperties success" );
         }

         @Override
         public void onFailure(Object context, Throwable var2) {
             log.error("reportProperties failed" + var2.toString());
         }   });

```

### 处理平台下发的属性读写
完整代码参见PropertySample.java
```java
    device.getClient().setPropertyListener(new PropertyListener() {

    //处理写属性
    @Override
    public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

        //遍历service
        for (ServiceProperty serviceProperty: services){

            log.info("OnPropertiesSet, serviceId =  " + serviceProperty.getServiceId());

            //遍历属性
            for (String name :serviceProperty.getProperties().keySet()){
                log.info("property name = "+ name);
                log.info("set property value = "+ serviceProperty.getProperties().get(name));
                if (name.equals("alarm")){
                    //修改本地值
                    alarm = (Integer) serviceProperty.getProperties().get(name);
                }
            }

        }
        //修改本地的属性值
        device.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
    }

    //处理读属性
    @Override
    public void onPropertiesGet(String requestId, String serviceId) {

        log.info("OnPropertiesGet " + serviceId);
        Map<String ,Object> json = new HashMap<>();
        Random rand = new Random();
        json.put("alarm", alarm);
        json.put("temperature", rand.nextFloat()*100.0f);
        json.put("humidity", rand.nextFloat()*100.0f);
        json.put("smokeConcentration", rand.nextFloat() * 100.0f);

        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setProperties(json);
        serviceProperty.setServiceId("smokeDetector");

        device.getClient().respondPropsGet(requestId, Arrays.asList(serviceProperty));
    }
});

```

### 处理平台下发的命令
完整代码参见CommandSample.java
```java
    client.setCommandListener(new CommandListener() {
    @Override
    public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
        log.info("onCommand, serviceId = " +serviceId);
        log.info("onCommand , name = " + commandName);
        log.info("onCommand, paras =  " + paras.toString());

        //处理命令

        //发送命令响应
        device.getClient().respondCommand(requestId, new CommandRsp(0));
    }   });

```

### 面向物模型编程
面向物模型编程指的是，基于SDK提供的物模型抽象能力，设备代码只需要按照物模型定义设备服务，然后就可以直接访问设备服务，SDK就能自动的和平台通讯，完成属性的同步和命令的调用。
相比直接调用客户端接口和平台进行通讯，面向物模型编程简化了设备侧代码的复杂度，让设备代码只需要关注业务，而不用关注和平台的通讯过程。


首先定义一个烟感服务类，继承自AbstractService
```java
    public static class SmokeDetectorService extends AbstractService {
	}

```
定义服务属性，属性和产品模型保持一致。writeable用来标识属性是否可写
```java
    @Property(name = "alarm", writeable = true)
    int smokeAlarm = 0;

    @Property(name = "smokeConcentration", writeable = false)
    float concentration = 0.0f;

    @Property(writeable = false)
    int humidity;

    @Property(writeable = false)
    float temperature;
```

定义属性的读写接口：
getter接口为读接口，在属性上报和平台主动查属性时被sdk调用
setter接口为写接口，在平台修改属性时被sdk调用，如果属性是只读的，则setter接口保留空实现。
```java	
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

```

定义服务的命令：
命令的入参和返回值类型固定不能修改。
```java	

    @DeviceCommand(name = "ringAlarm")
    public CommandRsp alarm(Map<String, Object> paras) {
        int duration = (int) paras.get("duration");
        log.info("ringAlarm  duration = " + duration);
        return new CommandRsp(0);
    }
```	

上面完成了服务的定义
接下来创建设备，注册烟感服务，然后初始化设备：
```java
    //创建设备
   IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
           "5e06bfee334dd4f33759f5b3_demo", "mysecret");

   //创建设备服务
   SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
   device.addService("smokeDetector", smokeDetectorService);

   if (device.init() != 0) {
       return;
   }

```

启动服务属性自动周期上报
```java
    smokeDetectorService.enableAutoReport(10000);

```

### 使用设备代码生成器
上面基于物模型编程中，要求服务的定义必须和产品模型保持一致，基于这一点，我们提供了代码生成器，能根据产品模型自动生成设备代码。
代码生成器的源码在iot-device-code-generator目录
[具体参见](https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/tree/master/iot-device-code-generator/README.md)
           
    


### 使用证书认证
完整代码参见X509CertificateDeviceSample.java

首选读取证书，如果是pem格式的证书：
```java
    KeyStore keyStore = DemoUtil.getKeyStore("D:\\SDK\\cert\\deviceCert.pem", "D:\\SDK\\cert\\deviceCert.key", "keypassword");
   
```
如果是keystore格式证书：
```java
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "keystorepassword".toCharArray());
```

然后使用证书创建设备
```java
    IoTDevice iotDevice = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo3", keyStore, "keypassword");
```


## License
SDK的开源License类型为 [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause)。详情参见LICENSE.txt

## 如何贡献代码
1、创建github账号
2、fork huaweicloud-iot-device-sdk-java源代码
3、同步huaweicloud-iot-device-sdk-java主仓库代码到fork的仓库
4、在本地修改代码并push到fork的仓库
5、在fork的仓库提交pull request到主仓库
  
[更多文档](https://support.huaweicloud.com/usermanual-IoT/iot_01_0006.html)



