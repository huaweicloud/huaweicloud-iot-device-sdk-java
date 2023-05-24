English | [简体中文](./README.md) 

# huaweicloud-iot-device-sdk-java

huaweicloud-iot-device-sdk-java provides the Java SDK for devices to connect to HUAWEI CLOUD IoT platform. It provides communication capabilities between devices and the platform and advanced services such as device services, gateway services, and over-the-air (OTA). In addition, huaweicloud-iot-device-sdk-java provides rich demo code for various scenarios. IoT device developers can use SDKs to greatly simplify development and quickly access the platform.


* [Supported Features](#supported-features)
* [How to use](#how-to-use)
* [Device Initialization](#device-initialization)
* [Report Message](#report-message)
* [Report device properties](#report-device-properties)
* [Processing commands delivered by the platform](#processing-commands-delivered-by-the-platform)
* [Object-oriented model programming](#object-oriented-model-programming)
* [Use Device Code Generator](#use-device-code-generator)
* [Certificate authentication](#certificate-authentication)
* [Version Update Description](#version-update-description)
* [Interface Document](#https://cn-north-4-iot-sp.huaweicloud.com/assets/helpcenter/doc/index.html)
* [More Documents](https://support.huaweicloud.com/devg-iothub/iot_02_0089.html)
* [License](#license)
* [How to contribute code](#how-to-contribute-code)

## Supported Features
- Supports device messages, property reporting, property reading and writing, and command delivery.
- Gateway service, subdevice management, and subdevice message forwarding.
- Supports device OTA services.
- Object-oriented programming.
- Provides a device code generator to automatically generate device codes based on product models.
- Supports password authentication and certificate authentication.
- User-defined topics are supported.

## How to use

Dependent version:
* JDK ：1.8 +

### Device Initialization

Create and initialize a device. Currently, the device supports the communication with Chinese cryptographic algorithms.
Before enabling the Chinese encryption algorithm, see the [BGMProvider Installation Guide](https://gitee.com/openeuler/bgmprovider/wikis/English%20Documentation/BGMProvider%20Installation%20Guide)

```java
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "mysecret", file);
        // By default, international encrypted communication is used. To use Chinese encrypted communication, setGmssl to true.
        //device.getClient().getClientConf().setGmssl(true);
        // By default, the timestamp is not verified. To verify the timestamp, set the corresponding parameter to Hash algorithm.
        //device.getClient().getClientConf().setCheckStamp(Constants.CHECK_STAMP_SM3_ON);
        if (device.init() != 0) {
            return;
        }
```

### Report message

Report device information：
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

Report the user-defined topic message. (Note that the user-defined topic needs to be configured on the platform first.)
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
For details about the complete code, see MessageSample.java.				

### Report device properties

```java
     Map<String ,Object> json = new HashMap<>();
     Random rand = new Random();

     // Set properties based on object models.
     json.put("alarm", alarm);
     json.put("temperature", rand.nextFloat()*100.0f);
     json.put("humidity", rand.nextFloat()*100.0f);
     json.put("smokeConcentration", rand.nextFloat() * 100.0f);

     ServiceProperty serviceProperty = new ServiceProperty();
     serviceProperty.setProperties(json);
     serviceProperty.setServiceId("smokeDetector");// The value of serviceId must be the same as that in the object model.

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
For the complete code, see propertiesample.java.

### Report subdevice properties
```java
     Map<String ,Object> json = new HashMap<>();
     Random rand = new Random();
     String subdeviceId = "xxxxx";

     // Set properties based on object models.
     json.put("alarm", alarm);
     json.put("temperature", rand.nextFloat()*100.0f);
     json.put("humidity", rand.nextFloat()*100.0f);
     json.put("smokeConcentration", rand.nextFloat() * 100.0f);

     ServiceProperty serviceProperty = new ServiceProperty();
     serviceProperty.setProperties(json);
     serviceProperty.setServiceId("smokeDetector");// The value of serviceId must be the same as that in the object model.

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

### Processing the read and write of properties delivered by the platform

```java
    device.getClient().setPropertyListener(new PropertyListener() {

    // Process write properties
    @Override
    public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

        // Traverse services.
        for (ServiceProperty serviceProperty: services){

            log.info("OnPropertiesSet, serviceId is {}", serviceProperty.getServiceId());

            // Traverse properties.
            for (String name :serviceProperty.getProperties().keySet()){
                log.info("property name is {}", name);
                log.info("set property value is {}", serviceProperty.getProperties().get(name));
                if (name.equals("alarm")){
                    // Modifying Local Values
                    alarm = (Integer) serviceProperty.getProperties().get(name);
                }
            }

        }
        // Modifying Local property Values
        device.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
    }

    // Process Read properties
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
For the complete code, see propertiesample.java.

### Processing commands delivered by the platform

```java
    client.setCommandListener(new CommandListener() {
    @Override
    public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
        log.info("onCommand, serviceId is {}", serviceId);
        log.info("onCommand , name is {}", commandName);
        log.info("onCommand, paras is {}", paras.toString());

        // Processing Commands

        // Send command response
        device.getClient().respondCommand(requestId, new CommandRsp(0));
    }   });

```
For the complete code, see CommandSample.java.

### Object-oriented model programming
Object model-oriented programming refers to the object model abstraction capability provided by the SDK. The device code only needs to define device services based on the object model. The SDK can automatically communicate with the platform to synchronize properties and invoke commands.
Compared with directly invoking the client interface to communicate with the platform, object-oriented programming simplifies the code complexity on the device side. In this way, the device code only needs to focus on services instead of the communication process with the platform.


Define a smoke sensor service class, which is inherited from AbstractService.

```java
    public static class SmokeDetectorService extends AbstractService {
	}

```
Define service properties. The properties are consistent with those in the product model. writeable: indicates whether an property is writable.
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

Define the read/write interface of the property.
The getter interface is a read interface and is invoked by the SDK when properties are reported or the platform proactively queries properties.
The setter interface is a write interface and is invoked by the SDK when the platform modifies an property. If the property is read-only, the setter interface is left empty.

```java	
        public int getHumidity() {

            // Simulate reading data from the sensor.
            humidity = new Random().nextInt(100);
            return humidity;
        }

        public void setHumidity(int humidity) {
            // humidity is read-only and does not need to be implemented.
        }

        public float getTemperature() {

            // Simulate reading data from the sensor.
            temperature = new Random().nextInt(100);
            return temperature;
        }

        public void setTemperature(float temperature) {
            // The set interface does not need to be implemented for read-only fields.
        }

        public float getConcentration() {

            // Simulate reading data from the sensor.
            concentration = new Random().nextFloat()*100.0f;
            return concentration;
        }

        public void setConcentration(float concentration) {
            // The set interface does not need to be implemented for read-only fields.
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

Define Command for of service:
The input parameter and return value types of the command are fixed and cannot be changed.

```java	

    @DeviceCommand(name = "ringAlarm")
    public CommandRsp alarm(Map<String, Object> paras) {
        int duration = (int) paras.get("duration");
        log.info("ringAlarm  duration = " + duration);
        return new CommandRsp(0);
    }
```

The service is defined above.
Then create a device, register the smoke sensor service, and initialize the device.

```java
    // Creating a Device
   IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
           "5e06bfee334dd4f33759f5b3_demo", "mysecret", file);

   // Create a device service.
   SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
   device.addService("smokeDetector", smokeDetectorService);

   if (device.init() != 0) {
       return;
   }

```

Enabling the Automatic Periodic Reporting of Service properties
```java
    smokeDetectorService.enableAutoReport(10000);

```

### Use Device Code Generator
In the preceding object model-based programming, the service definition must be consistent with the product model. Based on this, we provide a code generator to automatically generate device code based on the product model.
The source code of the code generator is stored in the iot-device-code-generator directory.
[For details](https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/tree/master/iot-device-code-generator/README_EN.md)


### Certificate authentication
For the complete code, see X509CertificateDeviceSample.java.

Preferentially read the certificate. If the certificate is in PEM format, run the following command:

```java
    KeyStore keyStore = DemoUtil.getKeyStore("D:\\SDK\\cert\\deviceCert.pem", "D:\\SDK\\cert\\deviceCert.key", "keypassword");
   
```
If the certificate is in keystore format:
```java
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "keystorepassword".toCharArray());
```
If devices are connected using Chinese cryptographic algorithms, you need to import two certificates.
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

Then use the certificate to create the device
```java
    IoTDevice iotDevice = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo3", keyStore, "keypassword", file);
```

### The device uses the authoritative CA authentication platform.
Currently, the platform uses certificates issued by two authoritative CAs: [DigiCert Global Root CA.](https://global-root-ca.chain-demos.digicert.com/info/index.html) and [GlobalSign Root CA - R3](https://valid.r3.roots.globalsign.com/).

For details, see section[Certificate Resources](https://support.huaweicloud.com/intl/en-us/devg-iothub/iot_02_1004.html)in the official document.

The [iot-device-demo/src/main/resources/rootca](iot-device-demo/src/main/resources/rootca) directory of this code repository provides combinations of various formats and individual CA certificate files.

When you connect to multiple devices on the platform, you are advised to use the combined root CA certificate file to ensure programming interface consistency. (huaweicloud-iot-root-ca-list.bks\huaweicloud-iot-root-ca-list.jks\huaweicloud-iot-root-ca-list.pem, that is, ca.jks in each sample project).

## Version Update Description

| Version | Change Type           | Description                                                  |
| ------- | --------------------- | ------------------------------------------------------------ |
| 1.2.0   | New functions         | Added the functions of generic protocols, Chinese national cryptographic algorithm, and OBS upgrade packages. |
|         | Function enhancements | 1. The method of passing the platform root CA certificate to the BootstrapClient constructor is optimized. The original constructor is marked as deprecated. <br/>2. Update ca.jks in the Samples file to the certificate file that contains all authoritative root CA certificates of the device side of the IoT platform instances in each region. <br/>3. Fixed some spelling errors. <br/>4. Upgrade the Paho. <br/>5. Fixed an issue where no retry is performed after a long time of backoff reconnection. |
| 1.1.2   | Function enhancements | The provisioning function is modified to be compatible with scenario of different certificate in multiple regions. |
| 1.0.1   | New functions         | The functions of some added interfaces are as follows: <br/>The implicit subscription interface and data compression reporting interface are added. |
| 1.0.0   | Function enhancements | 1. Modify the logic to be compatible with the old V3 interface.<br/>2. Update the subdevice status on the gateway.<br/>3. Modify the QoS of the default subscription topic, modify the reconnection of new links, squeeze old links, and modify the reconnection time. |
| 0.8.0   | Function enhancements | Replace the access domain name (iot-mqtts.cn-north-4.myhuaweicloud.com) and root certificate with a new one. <br/>If the device uses the old domain name (iot-acc.cn-north-4.myhuaweicloud.com) for access, use SDK v0.6.0 or an earlier version. |
| 0.6.0   | Function enhancements | Adjust the OTA service usage mode and optimize the MD.       |
| 0.5.0   | New functions         | Provide ability of interconnecting with HUAWEI CLOUD IoT platform, to implement service scenarios such as access, device management, and command delivery. |

1. The compression interface is added.
2. Implicit subscription.
3. Reliability embedment and backoff reconnection.
4. If the username or password is incorrect, no reconnection is required.
5. Time synchronization.
6. Code refactoring.
7. Report device logs.
8. The gateway actively manages subdevices (adding or deleting subdevices).
9. Supports the IoTDA device provisioning process.
10. The SDK log component is modified.
11. The message provisioning function is added.
12. Modified the provisioning function.
13. Compatible with scenario of different certificates in multiple region.
14. Reporting device information.
15. Open-source component upgrade.
16. Add the integrated root CA certificate.
17. Upgrade paho.
18. add generic protocols 
19. Fixed an issue where no retry is performed after a long time of backoff reconnection.
20. OBS upgrade packages are supported.
21. Support for Chinese national cryptographic algorithm.

*2023/5/15*

Download release version at https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/releases

## License

The open-source license type of the SDK is  [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause)。For details, see the LICENSE.txt file.

## How to contribute code

1. Create a GitHub account.
2. Fork huaweicloud-iot-device-sdk-java Source Code
3. Synchronize the huaweicloud-iot-device-sdk-java main repository code to the fork repository.
4. Modify the code locally and push it to the fork repository.
5. Submit a pull request from the fork repository to the main repository.