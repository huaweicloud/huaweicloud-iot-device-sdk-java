::请修改自己的参数，productZip为产品模型包路径
set  productZip=".\iot-device-code-generator\src\main\resources\smokeDetector_cb097d20d77b4240adf1f33d36b3c278_smokeDetector.zip"
::修改为自己的接入域名
set  accessAddress="000000000.st1.iotda-device.cn-south-1.myhuaweicloud.com"
set  deviceId="5e06bfee334dd4f33759f5b3_demo"
set  secret="******"
cd ..
call mvn clean install
call java -jar iot-device-code-generator\target\iot-device-code-generator-1.2.0-with-deps.jar %productZip%
cd generated-demo
call mvn install
call java -jar target\iot-device-demo-ganerated-1.2.0-with-deps.jar %accessAddress% %deviceId% %secret%
pause

