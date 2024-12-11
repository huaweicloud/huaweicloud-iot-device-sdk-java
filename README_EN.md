[English](./README_EN.md) | Simplified Chinese

# huaweicloud-iot-device-sdk-java

huaweicloud-iot-device-sdk-java provides abundant demo code for devices to communicate with the platform and implement device, gateway, and over-the-air (OTA) services. The SDK greatly simplifies device development and enables quick access to the platform.


* [Supported Features](#Supported Features)
* [How to Use](#How to Use)
* [Initializing a Device](#Initializing a Device)
* [Reporting a Message](#Reporting a Message)
* [Reporting Device Properties](#Reporting Device Properties)
* [Processing Read and Write on Properties Delivered by the Platform](#Processing Read and Write on Properties Delivered by the Platform)
* [Processing Commands Delivered by the Platform](#Processing Commands Delivered by the Platform)
* [Profile-oriented Programming](#Profile-oriented Programming)
* [Using the Device Code Generator](#Using the Device Code Generator)
* [Using a Certificate for Authentication](#Using a Certificate for Authentication)
* [Version Updates](#Version Updates)
* [API Reference](https://cn-north-4-iot-sp.huaweicloud.com/assets/helpcenter/doc/index.html)
* [More Documents](https://support.huaweicloud.com/intl/en-us/devg-iothub/iot_02_0089.html)
* [License](#License)
* [How to Contribute Code](#How to Contribute Code)

## Supported Features
- Device message reporting, property reporting, property reading and writing, and command delivery
- Gateway services, child device management, and child device message forwarding
- Device OTA services
- Profile-oriented programming
- Automatic device code generation based on product models using the device code generator
- Device authentication using secrets and certificates
- Topic customization

## How to Use

Dependent versions:
* JDK 1.8 or later



### Initializing a Device

Create and initialize a device. Currently, communication secured with Chinese cryptography algorithms is supported.
Before enabling the Chinese cryptography algorithms, see the [BGMProvider Installation Guide](https://gitee.com/openeuler/bgmprovider/wikis/%E4%B8%AD%E6%96%87%E6%96%87%E6%A1%A3/BGMProvider%E5%AE%89%E8%A3%85%E6%8C%87%E5%8D%97).
```java
        IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "your device id", "your device secret", file);
        //International encrypted communication is used by default. To use Chinese encrypted communication, set Gmssl to true.
        //device.getClient().getClientConf().setGmssl(true);
        //The timestamp is not verified by default. To verify the timestamp, set the corresponding parameter to select the hash algorithm.
        //device.getClient().getClientConf().setCheckStamp(Constants.CHECK_STAMP_SM3_ON);
        if (device.init() != 0) {
            return;
        }
```

### Reporting a Message

Report a device message.
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

Report custom topic messages (starting with $oc, note that custom topics need to be configured on the platform first)
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
For the complete code, see **MessageSample.java**.

### Reporting Device Properties

```java
     Map<String ,Object> json = new HashMap<>();
     Random rand = new Random();

     // Set properties based on the product model.
     json.put("alarm", alarm);
     json.put("temperature", rand.nextFloat()*100.0f);
     json.put("humidity", rand.nextFloat()*100.0f);
     json.put("smokeConcentration", rand.nextFloat() * 100.0f);

     ServiceProperty serviceProperty = new ServiceProperty();
     serviceProperty.setProperties(json);
     serviceProperty.setServiceId("smokeDetector");// The service ID must be consistent with that defined in the product model.

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
For the complete code, see **PropertySample.java**.

### Reporting Child Device Properties
```java
     Map<String ,Object> json = new HashMap<>();
     Random rand = new Random();
     String subdeviceId = "xxxxx";

     // Set properties based on the product model.
     json.put("alarm", alarm);
     json.put("temperature", rand.nextFloat()*100.0f);
     json.put("humidity", rand.nextFloat()*100.0f);
     json.put("smokeConcentration", rand.nextFloat() * 100.0f);

     ServiceProperty serviceProperty = new ServiceProperty();
     serviceProperty.setProperties(json);
     serviceProperty.setServiceId("smokeDetector");// The service ID must be consistent with that defined in the product model.

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

### Processing Read and Write on Properties Delivered by the Platform

```java
    device.getClient().setPropertyListener(new PropertyListener() {

    // Process property writing.
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
                    // Change the local value.
                    alarm = (Integer) serviceProperty.getProperties().get(name);
                }
            }

        }
        // Change the local property value.
        device.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
    }

    // Process property reading.
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
For the complete code, see **PropertySample.java**.

### Processing Commands Delivered by the Platform

```java
    client.setCommandListener(new CommandListener() {
    @Override
    public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
        log.info("onCommand, serviceId is {}", serviceId);
        log.info("onCommand , name is {}", commandName);
        log.info("onCommand, paras is {}", paras.toString());

        // Process a command.

        // Send a command response.
        device.getClient().respondCommand(requestId, new CommandRsp(0));
    }   });

```
For the complete code, see **CommandSample.java**.

### Profile-oriented Programming
You can use the profile capabilities provided by the SDK to define device services. The SDK can automatically communicate with the platform to synchronize properties and call commands.
Profile-oriented programming simplifies device code and enables you to focus only on services rather than the communications with the platform. This method is much easier than calling client APIs.


Define a smoke sensor service class, which is inherited from **AbstractService**.
```java
    public static class SmokeDetectorService extends AbstractService {
	}

```
Define service properties, which must be consistent with those defined in the product model. **writeable** indicates whether the property is writable.
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

Define the methods for reading and writing properties.
The getter method is used for reading. It is called by the SDK when devices report properties and the platform queries properties.
The setter method is used for writing. It is called by the SDK when the platform modifies properties. If the properties are read-only, leave the setter method not implemented.

```java	
        public int getHumidity() {

            // Simulate the action of reading data from the sensor.
            humidity = new Random().nextInt(100);
            return humidity;
        }

        public void setHumidity(int humidity) {
            // You do not need to implement this method for read-only fields.
        }

        public float getTemperature() {

            // Simulate the action of reading data from the sensor.
            temperature = new Random().nextInt(100);
            return temperature;
        }

        public void setTemperature(float temperature) {
            // You do not need to implement this method for read-only fields.
        }

        public float getConcentration() {

            // Simulate the action of reading data from the sensor.
            concentration = new Random().nextFloat()*100.0f;
            return concentration;
        }

        public void setConcentration(float concentration) {
            // You do not need to implement this method for read-only fields.
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

Define the service command.
The type of command input parameters and return values cannot be changed.

```java	

    @DeviceCommand(name = "ringAlarm")
    public CommandRsp alarm(Map<String, Object> paras) {
        int duration = (int) paras.get("duration");
        log.info("ringAlarm  duration = " + duration);
        return new CommandRsp(0);
    }
```

After the service is defined,
create a device, register the smoke sensor service, and initialize the device.
```java
    // Create a device.
   IoTDevice device = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
           "5e06bfee334dd4f33759f5b3_demo", "mysecret", file);

   // Create a device service.
   SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
   device.addService("smokeDetector", smokeDetectorService);

   if (device.init() != 0) {
       return;
   }

```

Enable periodic property reporting.
```java
    smokeDetectorService.enableAutoReport(10000);

```

### Using the Device Code Generator
As the service you define is consistent with that in the product model in profile-oriented programming, you can use the code generator to generate device code automatically based on the product model.
The source code of the code generator is stored in the **iot-device-code-generator** directory.
[See details](https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/tree/master/iot-device-code-generator/README.md).


### Using a Certificate for Authentication
For the complete code, see **X509CertificateDeviceSample.java**.

Obtain a certificate. For a PEM certificate:
```java
    KeyStore keyStore = DemoUtil.getKeyStore("D:\\SDK\\cert\\deviceCert.pem", "D:\\SDK\\cert\\deviceCert.key", "keypassword");
   
```
For a Keystore certificate:
```java
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(new FileInputStream("D:\\SDK\\cert\\my.keystore"), "keystorepassword".toCharArray());
```
For device access in Chinese cryptographic algorithm scenario, import two certificates:
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

Use the certificate to create a device.
```java
    IoTDevice iotDevice = new IoTDevice("ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo3", keyStore, "keypassword", file);
```

### Using a Certificate for Authentication
Currently, the IoT platform uses certificates issued by two authoritative CAs: [DigiCert Global Root CA.](https://cacerts.digicert.com/DigiCertGlobalRootCA.crt.pem) and [GlobalSign Root CA - R3](https://valid.r3.roots.globalsign.com/).

For details, see section [Certificates](https://support.huaweicloud.com/intl/en-us/devg-iothub/iot_02_1004.html#section3).

The [iot-device-demo/src/main/resources/rootca](iot-device-demo/src/main/resources/rootca) directory of this code repository provides combinations of various formats and separate CA certificate files.

When you connect multiple devices to the IoT platform, you are advised to use the combined root CA certificate file (huaweicloud-iot-root-ca-list.bks\huaweicloud-iot-root-ca-list.jks\huaweicloud-iot-root-ca-list.pem, that is, ca.jks in each sample project) to ensure the consistency of programming interfaces.

## Version Updates

| Version Number| Change Type| Description                                                        |
| ------ | -------- | ------------------------------------------------------------ |
| 1.2.0  | New features| The generic protocol, Chinese cryptographic algorithms, and OBS upgrade packages were added.                         |
|        | Function enhancement| 1. Optimized the method of transferring the platform root CA certificate to the BootstrapClient construction method . The original construction method was marked as discarded.<br>2. Updated **ca.jks** in Samples to all authoritative root CA certificates that contain device certificates of instances in each region.<br>3. Fixed some spelling mistakes.<br>4. Upgraded paho.<br>5. Fixed the issue where the system did not retry after a backoff reconnection.|
| 1.1.2  | Function enhancement| Modified the provisioning function and compatible with scenarios where different certificates are used in multiple regions.                |
| 1.0.1  | New feature  | The implicit subscription interface and data compression reporting interface were added.                      |
| 1.0.0  | Function enhancement| 1. Modified the logic for compatibility with old V3 interfaces.<br>2. The subdevice status was refreshed by gateway.<br>3. Modified the QoS of the default subscription topic, modified the conflict between a new link and an old link, and modified the reconnection time.|
| 0.8.0  | Function enhancement| Added the access domain name (iot-mqtts.cn-north-4.myhuaweicloud.com) and root certificate.<br>If the device uses the old domain name (iot-acc.cn-north-4.myhuaweicloud.com) for access, use the SDK of v0.6.0 or an earlier version.|
| 0.6.0  | Function enhancement| Adjusted the OTA service use and improved the MD.                                 |
| 0.5.0  | New feature| Connected to the Huawei Cloud IoT platform to facilitate service scenarios such as access, device management, and command delivery.|

1. Added compression APIs.

2. Supported implicit subscription.

3. Supported reliability embedding and backoff reconnection.

4. Supported reconnection denial due to incorrect username or password.

5. Supported time synchronization.

6. Supported code refactoring.

7. Supported device log reporting.

8. Supported the function that gateways proactively manage (add or delete) child devices.

9. Supported IoTDA device provisioning.

10. Modified the SDK log components.

11. Added the message provisioning function.

12. Modified the provisioning function.

13. Supported the compatibility with different certificates in multiple regions.

14. Supported device information reporting.

15. Upgraded open-source components.

16. Added the combined root CA certificates.

17. Upgraded paho.

18. Added generic protocols.

19. Fixed an issue where the system did not retry after a backoff reconnection.

20. Supported OBS upgrade package.

21. Supported Chinese cryptographic algorithms.

*2023/5/15*

Download the release version from [https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/releases](https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java/releases).

## License

The open-source SDK license type is [BSD 3-Clause License](https://opensource.org/licenses/BSD-3-Clause). For details, see **LICENSE.txt**.

## How to Contribute Code

1. Create a GitHub account.
2. Fork the huaweicloud-iot-device-sdk-java source code.
3. Synchronize the code from the huaweicloud-iot-device-sdk-java main repository to the fork repository.
4. Modify the code locally and push the code to the fork repository.
5. Submit a pull request to the main repository.
