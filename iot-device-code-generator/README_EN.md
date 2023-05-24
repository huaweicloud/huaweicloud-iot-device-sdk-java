# iot-device-code-generator

iot-device-code-generator generates device code automatically, simplifying device code development. You can use it to generate the device code framework based on the product model file.


## How to Use
### Using the Code Generator to Generate Device Code
1. Download the **huaweicloud-iot-device-sdk-java** project, decompress it, go to the **huaweicloud-iot-device-sdk-java** directory, and run the **mvn install** command. 
  Check whether an executable JAR package is generated in the **target** folder of **iot-device-code-generator**. 

2. Save the product model to a local directory. For example, save the **smokeDetector_cb097d20d77b4240adf1f33d36b3c278_smokeDetector.zip** file to drive D. 
3. Run the following command in the **iot-device-code-generator\target\ **directory: 
  **java -jar iot-device-code-generator-0.2.0-with-deps.jar D:\smokeDetector_cb097d20d77b4240adf1f33d36b3c278_smokeDetector.zip ** 
  Use a correct version number for the JAR package name. 

If the **generated-demo** package is generated in the **huaweicloud-iot-device-sdk-java** directory, the device code is generated. 


### Compiling and Runing the Generated Device Code
1. Go to the **huaweicloud-iot-device-sdk-java\generated-demo** directory and run the **mvn install** command. 
  Generate a JAR package in the **target** directory. 
2. Run the **java -jar target\iot-device-demo-generated-0.2.0-with-deps.jar 5e06bfee334dd4f33759f5b3_demo** ***\**\** command.    
  Set the two parameters to the device ID and password, respectively.

Alternatively, open the **device-code-generator.bat** script in the **iot-device-code-generator** directory, modify the preceding parameters, and double-click the script to run it. 
​    
