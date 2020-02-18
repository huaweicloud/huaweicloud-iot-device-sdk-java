::请修改自己的参数，productZip为产品模型包路径
set  productZip="D:\Smoke_cb097d20d77b4240adf1f33d36b3c278_abc.zip"  
set  deviceId="5e06bfee334dd4f33759f5b3_demo"
set  secret="******"
git clone https://github.com/huaweicloud/huaweicloud-iot-device-sdk-java
cd huaweicloud-iot-device-sdk-java
call mvn clean install
call java -jar iot-device-code-generator\target\iot-device-code-generator-0.2.0-with-deps.jar %productZip% 
cd generated-demo
call mvn install
call java -jar target\iot-device-demo-ganerated-0.2.0-with-deps.jar %deviceId% %secret%
pause

