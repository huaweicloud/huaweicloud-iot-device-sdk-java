# iot-device-code-generator

iot-device-code-generator提供设备代码自动生成功能，用户只需要提供设备的产品模型文件，就可以自动生成设备代码框架，简化设备代码开发工作量。


## 如何使用
1、下载huaweicloud-iot-device-sdk-java工程，解压缩后进入huaweicloud-iot-device-sdk-java目录执行mvn install  
2、执行java -jar iot-device-code-generator\target\iot-device-code-generator-0.2.0-with-deps.jar D:\\Smoke_cb097d20d77b4240adf1f33d36b3c278_abc.zip 5e06bfee334dd4f33759f5b3_demo mysecret   
三个参数分别是：产品包zip文件、deviceId、secret  
在huaweicloud-iot-device-sdk-java目录下会生成generated-demo包  
3、进到huaweicloud-iot-device-sdk-java\generated-demo目录下执行mvn install  
4、执行java -jar target\iot-device-demo-ganerated-0.2.0-with-deps.jar 运行生成的demo  

如果觉得上面操作比较烦，可以打开iot-device-code-generator目录下的device-code-genarator.bat脚本，修改上面的参数，然后双击运行即可
    




