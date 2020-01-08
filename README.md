# huaweicloud-iot-device-sdk-java

huaweicloud-iot-device-sdk-java提供设备接入华为云IoT物联网平台的Java版本的SDK，提供设备和平台之间通讯能力，以及设备服务、网关服务、OTA等高级服务，并且针对各种场景提供了丰富的demo代码。IoT设备开发者使用SDK可以大大简化开发复杂度，快速的接入平台。

## 支持特性
- 支持设备消息、属性上报、属性读写、命令下发
- 支持网关服务、子设备管理、子设备消息转发
- 支持设备OTA服务
- 提供设备抽象服务，支持面向物模型编程
- 支持密码认证和证书认证两种设备认证方式


## 构建代码

依赖的版本：
* JDK ：1.8 +

运行如下命令:

	git clone https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java.git
	cd huaweicloud-iot-device-sdk-java
	mvn clean install

## 如何使用
因为huaweicloud-iot-device-sdk-java还没有发布到公共仓库，如果要使用，需要先下载代码在本地构建。
    
    mvn clean install 

项目中可以使用dependencyManagement引入依赖。

    <dependency>
                <groupId>com.huaweicloud.iot</groupId>
                <artifactId>iot-device-sdk-java</artifactId>
                <version>0.1.0</version>
    </dependency>
    
## 快速开始

创建设备并初始化
```java
        IoTDevice device = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret");
        if (device.init() != 0) {
            return;
        }
```

上报设备消息到平台(参见MessageSample)
```java

       device.getClient().publishDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            log.info("publishMessage success: ");
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            log.error("publishMessage fail: "+var2);
        }
    });

```

### 上报设备属性到平台(参见PropertySample)
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

### 处理平台下发的属性读写（参见PropertySample）
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

### 处理平台下发的命令（参见CommandSample）
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

定义烟感的服务类（参见DeviceServiceSample）
```java
    public static class SmokeDetectorService extends AbstractService {

    //按照设备模型定义属性，注意属性的name和类型需要和模型一致
    @Property(name = "alarm", writeable = true)
    int smokeAlarm = 0;

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

    //按照java bean规范自动生成setter和getter接口，sdk会自动调用这些接口
    public int getSmokeAlarm() {
        return smokeAlarm;
    }

    public void setSmokeAlarm(int smokeAlarm) {
        this.smokeAlarm = smokeAlarm;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getConcentration() {
        return concentration;
    }

    public void setConcentration(float concentration) {
        this.concentration = concentration;
    }

}

```

创建设备，注册烟感服务，然后初始化设备：
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

直接操作设备服务，SDK自动完成属性同步和命令调用：
```java
    Random rand = new Random();
    smokeDetectorService.setConcentration(rand.nextFloat() * 100.0f);
    smokeDetectorService.setTemperature(rand.nextFloat() * 100.0f);
    smokeDetectorService.setHumidity(rand.nextInt(100));
    smokeDetectorService.setSmokeAlarm(1);
    smokeDetectorService.firePropertiesChanged("smokeAlarm", "concentration");

```
### 使用证书认证（参见X509CertificateDeviceSample）

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


        
  
[更多文档]()



