# iot-device-code-generator

iot-device-code-generator提供设备代码自动生成功能，用户只需要提供设备的产品模型文件，就可以自动生成设备代码框架，简化设备代码开发工作量。


## 如何使用
### 使用使用代码生成器生成设备代码
1、下载huaweicloud-iot-device-sdk-java工程，解压缩后进入huaweicloud-iot-device-sdk-java目录执行mvn install  
执行完成会在iot-device-code-generator的target下生成可执行jar包  

2、将产品模型文件保存到本地，比如我的模型文件smokeDetector_cb097d20d77b4240adf1f33d36b3c278_smokeDetector.zip放到D盘  
3、进到 iot-device-code-generator\target\目录下执行  
  java -jar iot-device-code-generator-0.2.0-with-deps.jar D:\smokeDetector_cb097d20d77b4240adf1f33d36b3c278_smokeDetector.zip  
jar包名注意修改为正确的版本号  

在huaweicloud-iot-device-sdk-java目录下会生成generated-demo包  
至此，设备代码已经生成  

### 编译运行自动生成的设备代码
1、进到huaweicloud-iot-device-sdk-java\generated-demo目录下执行mvn install  
在target下生成jar包  
2、执行java -jar target\iot-device-demo-ganerated-0.2.0-with-deps.jar 5e06bfee334dd4f33759f5b3_demo *****     
两个参数分别为设备id和密码

如果觉得上面操作比较烦，可以打开iot-device-code-generator目录下的device-code-genarator.bat脚本，修改上面的参数，然后双击运行即可  
    




