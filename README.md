[English](./README_EN.md) | 简体中文

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
* [泛协议开发](#泛协议开发)
* [版本更新说明](#版本更新说明)
* [接口文档](https://cn-north-4-iot-sp.huaweicloud.com/assets/helpcenter/doc/index.html)
* [更多文档](https://support.huaweicloud.com/devg-iothub/iot_02_0089.html)
* [License](#License)
* [如何贡献代码](#如何贡献代码)

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



### 设备初始化

创建设备并初始化，当前已支持国密通信
启用国密前请参考[BGMProvider安装指南](https://gitee.com/openeuler/bgmprovider/wikis/%E4%B8%AD%E6%96%87%E6%96%87%E6%A1%A3/BGMProvider%E5%AE%89%E8%A3%85%E6%8C%87%E5%8D%97)进行配置
```java
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret", file);
        //默认使用国际加密通信，若要使用国密通信可setGmssl为true
        //device.getClient().getClientConf().setGmssl(true);
        //默认使用不校验时间戳，若要校验则设置对应的参数选择杂凑算法
        //device.getClient().getClientConf().setCheckStamp(Constants.CHECK_STAMP_SM3_ON);
        if (device.init() != 0) {
            return;
        }
```

### 上报消息

上报设备消息：
```java

       device.getClient().reportDeviceMessage(new DeviceMessage("hello"), new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            log.info("reportDeviceMessage success");
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            log.error("reportDeviceMessage fail: "+var2);
        }
    });

```

上报自定义topic消息（注意需要先在平台配置自定义topic）：
```java
		String topic = "$oc/devices/"+  device.getDeviceId() + "/user/wpy";
		device.getClient().publishRawMessage(new RawMessage(topic, "hello raw message "),
				new ActionListener() {
					@Override
					public void onSuccess(Object context) {
						log.info("publishRawMessage ok: ");
					}

					@Override
					public void onFailure(Object context, Throwable var2) {
						log.error("publishRawMessage fail: " + var2);
					}
				});
```
完整代码参见MessageSample.java					

### 上报设备属性

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
完整代码参见PropertySample.java

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

```java
    device.getClient().setPropertyListener(new PropertyListener() {

    //处理写属性
    @Override
    public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

        //遍历service
        for (ServiceProperty serviceProperty: services){

            log.info("OnPropertiesSet, serviceId is {}", serviceProperty.getServiceId());

            //遍历属性
            for (String name :serviceProperty.getProperties().keySet()){
                log.info("property name is {}", name);
                log.info("set property value is {}", serviceProperty.getProperties().get(name));
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

        log.info("OnPropertiesGet, the serviceId is {}", serviceId);
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
完整代码参见PropertySample.java

### 处理平台下发的命令

```java
    client.setCommandListener(new CommandListener() {
    @Override
    public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
        log.info("onCommand, serviceId is {}", serviceId);
        log.info("onCommand , name is {}", commandName);
        log.info("onCommand, paras is {}", paras.toString());

        //处理命令

        //发送命令响应
        device.getClient().respondCommand(requestId, new CommandRsp(0));
    }   });

```
完整代码参见CommandSample.java

### 面向物模型编程
面向物模型编程指的是，基于SDK提供的物模型抽象能力，设备代码只需要按照物模型定义设备服务，SDK就能自动的和平台通讯，完成属性的同步和命令的调用。
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
   IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
           "5e06bfee334dd4f33759f5b3_demo", "mysecret", file);

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
如果是国密场景下的设备接入，需要导入双证书：
```java
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null, null);
    GmCertificate gmSignCert = new GmCertificate("gm-sig-certificate", "D:\\devicecert\\gmcert_s\\CS.cert.pem",
                                                 "gm-sig-private-key", "D:\\devicecert\\gmcert_s\\CS.key.pem", "");
    GmCertificate gmEncCert = new GmCertificate("gm-enc-certificate", "D:\\devicecert\\gmcert_e\\CE.cert.pem",
                                                "gm-enc-private-key", "D:\\devicecert\\gmcert_e\\CE.key.pem", "");
    if(!CertificateUtil.getGmKeyStore(keyStore, gmSignCert) || !CertificateUtil.getGmKeyStore(keyStore, gmEncCert))
        return;
```

然后使用证书创建设备
```java
    IoTDevice iotDevice = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo3", keyStore, "keypassword", file);
```

### 设备使用权威CA认证平台
当前，平台使用了 [DigiCert Global Root CA.](https://global-root-ca.chain-demos.digicert.com/info/index.html) 和 [GlobalSign Root CA - R3](https://valid.r3.roots.globalsign.com/) 两个权威CA签发的证书。

官方文档的 [证书资源](https://support.huaweicloud.com/devg-iothub/iot_02_1004.html#section3) 章节提供了详细的说明。

本代码仓的 [iot-device-demo/src/main/resources/rootca](iot-device-demo/src/main/resources/rootca) 目录提供了各类格式的组合和单独的CA证书文件。

您在连接平台多个设备侧时，为了保持编程界面的一致性，我们建议您使用组合的根CA证书文件（huaweicloud-iot-root-ca-list.bks\huaweicloud-iot-root-ca-list.jks\huaweicloud-iot-root-ca-list.pem，即各Sample工程中的ca.jks）。


## 泛协议开发
目前平台支持基于MQTT/HTTP/LwM2M等标准协议接入，为解决用户自定义协议设备快速接入IoT平台的诉求。华为云IoT提供泛协议适配机制，您可使用泛协议对接SDK，快速构建协议插件，进行设备或平台与IoT的双向数据通信。

### SDK介绍
基于SDK实现泛协议设备接入的业务流程：

  ![](./doc/figure_cn/generic_sdk_1.png)
#### 监听平台下行数据的接口说明
| 接口 | 说明 |
| :---- | :---- |
| BridgeCommandListener | 平台命令下发监听接口。泛协议插件可以通过该接口将平台的下行命令转发给第三方协议设备。 |
| BridgeDeviceMessageListener | 平台消息下发监听接口。泛协议插件可以通过该接口将平台的下行消息转发给第三方协议设备。 |
| BridgeDeviceDisConnListener | 平台通知网桥断开设备连接监听接口。泛协议插件可以通过该接口主动断开第三方协议设备的连接。 |
| LoginListener | 网桥等待设备登录结果的监听接口。泛协议插件可以通过该接口监听设备是否登录成功。 |
| LogoutListener | 网桥等待设备登出结果的监听接口。泛协议插件可以通过该接口监听设备是否登出成功。 |

#### 相关类说明
| 类 | 说明 |
| :---- | :---- |
| BridgeClientConf | 泛协议SDK客户端配置类（包括泛协议SDK连接平台的地址、网桥ID、秘钥等参数） |
| BridgeBootstrap | 泛协议SDK启动初始化类。 |
| BridgeClient | 泛协议SDK网桥客户端实现类，实现同平台的通信（设备登录、设备消息上报、设备属性上报、设备登出等） |
  
### 使用 Bridge Demo
Brdige Demo提供了一个使用TCP设备接入网桥、于云平台进行交互的例子。下面将介绍Demo中各个部分的功能，帮您熟悉网桥开发要点。Demo项目结构图如下：

![](./doc/figure_cn/bridge_demo_pkg_structure.png)


相关类如下：

| 类名称 | 描述 |
| :---- | :---- |
| Main|主启动类。|
| BridgeService |网桥初始化：初始化同IoT平台的连接，设置平台下行数据监听|
| TcpServer |TCP协议服务端启动类。开启TCP协议监听端口，接收设备上报到服务端的消息。|
| MessageDecoder |上行数据的消息解码，将TCP原始码流转换为具体JSON对象。|
| MessageEncoder |下行数据的消息编码，将对象数据转换为TCP原始码流。|
| UpLinkHandler |设备上行数据处理类。把TCP协议数据转成平台格式数据，并调用SDK接口进行上报|
|DownLinkHandler|IoT平台下发数据处理类。将平台下发数据转换为TCP协议数据，并下发给设备。|
|DeviceSessionManger |设备会话管理。管理设备同服务端的连接。 |

#### 1. 初始化网桥SDK
创建BridgeBootstrap对象实例，调用InitBridge方法，在该方法中会读取环境变量的配置信息，并同IoT平台建立网桥连接。

**环境变量说明**
| 环境变量名称 | 参数说明 | 样例 |
| :---- | :---- | :---- |
| NET_BRIDGE_ID | 网桥ID | bridge001 |
| NET_NET_BRIDGE_SECRET | 网桥秘钥 | ******** |
| NET_NET_BRIDGE_SERVER_IP | IoTDA平台地址 | *****.iot-mqtts.cn-north-4.myhuaweicloud.com |
| NET_NET_BRIDGE_SERVER_PORT | IoTDA平台泛协议接入端口号 | 8883 |

初始化成功后，需要设置平台下行数据的监听器，监听平台的下行数据。

代码样例：
```java
public void init() {

    //网桥启动初始化
    BridgeBootstrap bridgeBootstrap = new BridgeBootstrap();

    // 从环境变量获取配置进行初始化
    bridgeBootstrap.initBridge();

    bridgeClient = bridgeBootstrap.getBridgeDevice().getClient();

    // 设置平台下行数据监听器
    DownLinkHandler downLinkHandler = new DownLinkHandler();
    bridgeClient.setBridgeCommandListener(downLinkHandler)   // 设置平台命令下发监听器
        .setBridgeDeviceMessageListener(downLinkHandler)    // 设置平台消息下发监听器
        .setBridgeDeviceDisConnListener(downLinkHandler);   // 设置平台通知网桥主动断开设备连接的监听器
}
  ```
#### 2. 设备登录上线
设备登录上线的实现样例如下：
```java
private void login(Channel channel, DeviceLoginMessage message) {
    int resultCode = BridgeService.getBridgeClient().loginSync(deviceId, secret, 5000);
    // 登录成功保存会话信息
    if (resultCode == 0) {
        deviceSession.setDeviceId(deviceId);
        deviceSession.setChannel(channel);
        DeviceSessionManger.getInstance().createSession(deviceId, deviceSession);
        NettyUtils.setDeviceId(channel, deviceId);
    }
}
```
设备上线时，需要从原始设备消息中解析出鉴权信息（设备ID和秘钥），再调用SDK提供的login接口向平台发起登录请求，平台收到设备的login请求后，会对设备的鉴权信息进行认证，认证通过后会通过返回码告知网桥SDK设备的登录结果。您需要根据登录结果对设备进行记录会话信息、给设备返回响应等处理。

#### 3. 设备数据上报
设备登录成功后，收到设备的上行数据时，可调用SDK的reportProperties将解码后的数据上报到IoT平台。

代码样例参考：
```java
private void reportProperties(Channel channel, BaseMessage message) {
    String deviceId = message.getMsgHeader().getDeviceId();
    DeviceSession deviceSession = DeviceSessionManger.getInstance().getSession(deviceId);
    if (deviceSession == null || !deviceSession.isLoginSuccess()) {
        log.warn("device={} is not login", deviceId);
        sendResponse(channel, message, 1);
        return;
    }
    // 调用网桥reportProperties接口，上报设备属性数据
    BridgeService.getBridgeClient()
        .reportProperties(deviceId, Collections.singletonList(serviceProperty), new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                sendResponse(channel, message, 0);
            }
            @Override
            public void onFailure(Object context, Throwable var2) {
                log.warn("device={} reportProperties failed: {}", deviceId, var2.getMessage());
                sendResponse(channel, message, 1);
            }
        });
}
```
#### 4. 平台指令下发

网桥在初始化时向SDK注册了BridgeCommandListener的监听。当有下行指令时，网桥SDK就会回调BridgeCommandListener的OnCommand方法。您可在OnCommand中对平台的下行指令进行处理。

代码样例参考：
```java
public void onCommand(String deviceId, String requestId, BridgeCommand bridgeCommand) {
    log.info("onCommand deviceId={}, requestId={}, bridgeCommand={}", deviceId, requestId, bridgeCommand);
    DeviceSession session = DeviceSessionManger.getInstance().getSession(deviceId);
    if (session == null) {
        log.warn("device={} session is null", deviceId);
        return;
    }

    // 设置位置上报的周期
    if (Constants.MSG_TYPE_FREQUENCY_LOCATION_SET.equals(bridgeCommand.getCommand().getCommandName())) {
        processLocationSetCommand(session, requestId, bridgeCommand);
    }
}
```
#### 5. 设备离线
网桥检查到设备到服务端的长连接断开时，需要调用SDK的logout接口通知平台设备离线。

代码样例参考：
```java
public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    String deviceId = NettyUtils.getDeviceId(ctx.channel());
    DeviceSessionManger.getInstance().getSession(deviceId);
    if (deviceId == null) {
        return;
    }
    // 调用网桥的logout接口，通知平台设备离线
    DefaultActionListenerImpl defaultLogoutActionListener = new DefaultActionListenerImpl("logout");
    BridgeService.getBridgeClient().logout(deviceId, UUID.randomUUID().toString(), defaultLogoutActionListener);
    DeviceSessionManger.getInstance().deleteSession(deviceId);

    ctx.close();
}
```
### 测试验证
### 1. 获取网桥接入信息
代码调试时，需要获取对应的网桥接入信息，并配置到对应的环境变量中。网桥接入信息，环境变量配置参考：
    
![](doc/figure_cn/bridge_get_auth_info.png)

### 2. 功能验证
均可参考[https://support.huaweicloud.com/usermanual-iothub/iot_02_3.html](https://support.huaweicloud.com/usermanual-iothub/iot_02_3.html) 实现

启动TCP：打开开文件`iot-bridge-demo\src\main\java\com\huaweicloud\sdk\iot\device\demo\TcpDevice.java`，将42行修改为：
```java
new TcpDevice("localhost", 8900).run();
```
启动工程模拟设备同网桥建立TCP连接，并发送登录请求。

## 版本更新说明

| 版本号 | 变更类型 | 说明                                                         |
| ------ | -------- | ------------------------------------------------------------ |
| 1.2.0  | 新增功能 | 新增泛协议、国密算法、OBS升级包功能                          |
|        | 功能增强 | 1、BootstrapClient构造方法传入平台根CA证书方式优化，原有构造方法标为已废弃；<br/>2、更新Samples中的ca.jks为包含平台各区域实例设备侧证书的所有权威根CA证书的证书文件；<br/>3、修复部分拼写错误;<br>4、paho升级;<br/>5、修复退避重连长时间后不再重试问题 |
| 1.1.2  | 功能增强 | 修改发放功能问题、兼容多region不同证书场景等                 |
| 1.0.1  | 新功能   | 增加隐式订阅接口、数据压缩上报接口等等                       |
| 1.0.0  | 功能增强 | 1、修改兼容V3旧接口逻辑<br/>2、网关刷新子设备状态<br/>3、修改默认订阅topic的qos、修改重连新链路挤老链路、修改重连时间 |
| 0.8.0  | 功能增强 | 更换新的接入域名（iot-mqtts.cn-north-4.myhuaweicloud.com）和根证书。<br/>如果设备使用老域名（iot-acc.cn-north-4.myhuaweicloud.com）接入，请使用 v0.6.0及以下版本的SDK |
| 0.6.0  | 功能增强 | 调整OTA服务使用方式；完善md                                  |
| 0.5.0  | 新增功能 | 提供对接华为云物联网平台能力，方便用户实现接入、设备管理、命令下发等业务场景 |

1、增加压缩接口

2、隐式订阅

3、可靠性预埋、退避重连

4、用户名、密码错误不重连

5、时间同步功能

6、代码重构

7、设备日志上报

8、网关主动管理子设备（子设备的添加/删除）

9、支持iotda发放设备流程

10、sdk日志组件更改

11、增加消息发放功能

12、修改发放功能问题

13、兼容多region不同证书场景

14、设备信息上报

15、开源组件升级

16、添加合一的根CA证书

17、升级paho

18、新增泛协议

19、修复退避重连长时间后不再重试问题

20、支持OBS升级包

21、支持国密算法

*2023/5/15*

release版本，请下载：https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/releases

## License

SDK的开源License类型为 [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause)。详情参见LICENSE.txt

## 如何贡献代码

1、创建github账号
2、fork huaweicloud-iot-device-sdk-java源代码
3、同步huaweicloud-iot-device-sdk-java主仓库代码到fork的仓库
4、在本地修改代码并push到fork的仓库
5、在fork的仓库提交pull request到主仓库