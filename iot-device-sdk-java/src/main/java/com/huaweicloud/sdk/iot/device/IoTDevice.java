package com.huaweicloud.sdk.iot.device;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.service.AbstractService;

import java.io.File;
import java.security.KeyStore;
import java.util.List;

/**
 * IOT设备类，SDK的入口类，提供两种使用方式：
 * 1、面向物模型编程：根据物模型实现设备服务，SDK自动完成设备和平台之间的通讯。这种方式简单易用，适合大多数场景
 * public class SmokeDetectorService extends AbstractService {
 *
 * @Property int smokeAlarm = 1;
 * <p>
 * public int getSmokeAlarm() {
 * //从设备读取属性
 * }
 * <p>
 * public void setSmokeAlarm(int smokeAlarm) {
 * //向设备写属性
 * }
 * }
 * <p>
 * //创建设备
 * IoTDevice device = new IoTDevice(serverUri, deviceId, secret);
 * //添加服务
 * SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
 * device.addService("smokeDetector", smokeDetectorService);
 * device.init();
 * <p>
 * <p>
 * 2、面向通讯接口编程：获取设备的客户端，直接和平台进行通讯。这种方式更复杂也更灵活
 * <p>
 * IoTDevice device = new IoTDevice(serverUri, deviceId, secret);
 * device.init();
 * device.getClient().reportDeviceMessage(new DeviceMessage("hello"),null);
 * device.getClient().setPropertyListener(....)
 * device.getClient().reportProperties(....)
 */
public class IoTDevice extends AbstractDevice {
    /**
     * 构造函数，使用密码创建设备
     *
     * @param serverUri    平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     * @param iotCertFile  iot平台的ca证书，用于双向校验时设备侧校验平台
     */
    public IoTDevice(String serverUri, String deviceId, String deviceSecret, File iotCertFile) {
        super(serverUri, deviceId, deviceSecret, iotCertFile);

    }

    /**
     * 构造函数，使用证书创建设备
     *
     * @param serverUri   平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId    设备id
     * @param keyStore    证书容器
     * @param keyPassword 证书密码
     * @param iotCertFile iot平台的ca证书，用于双向校验时设备侧校验平台
     */
    public IoTDevice(String serverUri, String deviceId, KeyStore keyStore, String keyPassword, File iotCertFile) {
        super(serverUri, deviceId, keyStore, keyPassword, iotCertFile);
    }

    /**
     * 构造函数，直接使用客户端配置创建设备，一般不推荐这种做法
     *
     * @param clientConf 客户端配置
     */
    public IoTDevice(ClientConf clientConf) {
        super(clientConf);
    }

    /**
     * 初始化，创建到平台的连接
     *
     * @return 此接口为阻塞调用，如果连接成功，返回0；否则返回-1
     */
    public int init() {
        return super.init();
    }

    /**
     * 添加服务。用户基于AbstractService定义自己的设备服务，并添加到设备
     *
     * @param serviceId     服务id，要和设备模型定义一致
     * @param deviceService 服务实例
     */
    public void addService(String serviceId, AbstractService deviceService) {
        super.addService(serviceId, deviceService);
    }

    /**
     * 查询服务
     *
     * @param serviceId 服务id
     * @return AbstractService 服务实例
     */
    public AbstractService getService(String serviceId) {
        return super.getService(serviceId);
    }

    /**
     * 触发属性变化，SDK会上报变化的属性
     *
     * @param serviceId  服务id
     * @param properties 属性列表
     */
    public void firePropertiesChanged(String serviceId, String... properties) {
        super.firePropertiesChanged(serviceId, properties);
    }

    /**
     * 触发多个服务的属性变化，SDK自动上报变化的属性到平台
     *
     * @param serviceIds 发生变化的服务id列表
     */
    public void fireServicesChanged(List<String> serviceIds) {
        super.fireServicesChanged(serviceIds);
    }

    /**
     * 获取直连设备客户端。获取到设备客户端后，可以直接调用客户端提供的消息、属性、命令等接口
     *
     * @return 设备客户端实例
     */
    public DeviceClient getClient() {
        return super.getClient();
    }
}
