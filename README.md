# huaweicloud-iot-device-sdk-java

huaweicloud-iot-device-sdk-java提供设备接入华为云IoT物联网平台的Java版本的SDK，提供设备和平台之间通讯能力，以及设备服务、网关服务、OTA等高级服务，并且针对各种场景提供了丰富的demo代码。IoT设备开发者使用SDK可以大大简化开发复杂度，快速的接入平台。

## 支持特性
- 支持设备消息、属性上报、属性读写、命令下发
- 支持网关服务、子设备管理、子设备消息转发
- 支持设备OTA服务
- 支持设备抽象服务，让设备可以基于物模型进行编程
- 支持MQTT协议和HTTP2协议
- 支持设备证书认证

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
    
[更多文档](https://github.com/huaweicloud/spring-cloud-huawei/blob/master/docs/index.md)



