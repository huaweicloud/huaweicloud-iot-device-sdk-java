package com.huaweicloud.sdk.iot.device;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.service.IService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import org.apache.log4j.Logger;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * IOT设备类，SDK的入口类，提供两种使用方式：
 * 面向物模型编程：用户根据物模型定义服务类并添加到设备，用户只需要对物的服务进行操作，SDK会自动完成服务属性的同步和命令调用。
 * 面向通讯接口编程：用户获取设备客户端实例，直接调用客户端提供的消息、属性、命令等接口和平台进行通讯
 */
public class IoTDevice {

    private Logger log = Logger.getLogger(IoTDevice.class);

    private DeviceClient client;
    private String deviceId;
    private Map<String, AbstractService> services = new HashMap<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private OTAService otaService;


    /**
     * 构造函数，使用密码创建设备
     *
     * @param serverUri    平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     */
    public IoTDevice(String serverUri, String deviceId, String deviceSecret) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(serverUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setSecret(deviceSecret);
        this.deviceId = deviceId;
        this.client = new DeviceClient(clientConf, this);
        this.otaService = new OTAService();
        this.addService("$ota", otaService);
        log.info("create device: " + clientConf.getDeviceId());

    }

    /**
     * 构造函数，使用证书创建设备
     *
     * @param serverUri   平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId    设备id
     * @param keyStore    证书容器
     * @param keyPassword 证书密码
     */
    public IoTDevice(String serverUri, String deviceId, KeyStore keyStore, String keyPassword) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(serverUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setKeyPassword(keyPassword);
        clientConf.setKeyStore(keyStore);
        this.deviceId = deviceId;
        this.client = new DeviceClient(clientConf, this);
        log.info("create device: " + clientConf.getDeviceId());
    }

    /**
     * 构造函数，直接使用客户端配置创建设备，一般不推荐这种做法
     *
     * @param clientConf 客户端配置
     */
    public IoTDevice(ClientConf clientConf) {
        this.client = new DeviceClient(clientConf, this);
        this.deviceId = clientConf.getDeviceId();
        log.info("create device: " + clientConf.getDeviceId());
    }

    /**
     * 初始化，创建到平台的连接
     *
     * @return 如果连接成功，返回0；否则返回-1
     */
    public int init() {
        return client.connect();
    }

    /**
     * 添加服务。用户基于AbstractService定义自己的设备服务，并添加到设备
     *
     * @param serviceId     服务id，要和设备模型定义一致
     * @param deviceService 服务实例
     */
    public void addService(String serviceId, AbstractService deviceService) {

        deviceService.setIotDevice(this);
        deviceService.setServiceId(serviceId);

        services.putIfAbsent(serviceId, deviceService);
    }


    /**
     * 查询服务
     *
     * @param serviceId 服务id
     * @return AbstractService 服务实例
     */
    public AbstractService getService(String serviceId) {

        return services.get(serviceId);
    }


    /**
     * 触发属性变化，SDK会上报变化的属性
     *
     * @param serviceId  服务id
     * @param properties 属性列表
     */
    public void firePropertiesChanged(String serviceId, String... properties) {
        AbstractService deviceService = getService(serviceId);
        if (deviceService == null) {
            return;
        }
        Map props = deviceService.onRead(properties);

        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId(deviceService.getServiceId());
        serviceProperty.setProperties(props);
        serviceProperty.setEventTime(IotUtil.getTimeStamp());

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                client.reportProperties(Arrays.asList(serviceProperty), new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("reportProperties failed: " + var2.toString());
                    }
                });
            }
        });

    }

    /**
     * 触发多个服务的属性变化，SDK自动上报变化的属性到平台
     *
     * @param serviceIds 发生变化的服务id列表
     */
    public void fireServicesChanged(List<String> serviceIds) {
        List<ServiceProperty> serviceProperties = new ArrayList<>();
        for (String serviceId : serviceIds) {
            AbstractService deviceService = getService(serviceId);
            if (deviceService == null) {
                log.error("service not found: " + serviceId);
                continue;
            }

            Map props = deviceService.onRead();

            ServiceProperty serviceProperty = new ServiceProperty();
            serviceProperty.setServiceId(deviceService.getServiceId());
            serviceProperty.setProperties(props);
            serviceProperty.setEventTime(IotUtil.getTimeStamp());
            serviceProperties.add(serviceProperty);
        }

        if (serviceProperties.isEmpty()) {
            return;
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                client.reportProperties(serviceProperties, new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        log.error("reportProperties failed: " + var2.toString());
                    }
                });
            }
        });
    }

    /**
     * 获取设备客户端。获取到设备客户端后，可以直接调用客户端提供的消息、属性、命令等接口
     *
     * @return 设备客户端实例
     */
    public DeviceClient getClient() {
        return client;
    }


    /**
     * 查询设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }


    /**
     * 命令回调函数，由SDK自动调用
     *
     * @param requestId 请求id
     * @param command   命令
     */
    public void onCommand(String requestId, Command command) {


        IService service = getService(command.getServiceId());

        if (service != null) {
            CommandRsp rsp = service.onCommand(command);
            client.respondCommand(requestId, rsp);
        }

    }

    /**
     * 属性设置回调，，由SDK自动调用
     *
     * @param requestId 请求id
     * @param propsSet  属性设置请求
     */
    public void onPropertiesSet(String requestId, PropsSet propsSet) {


        for (ServiceProperty serviceProp : propsSet.getServices()) {
            IService deviceService = getService(serviceProp.getServiceId());

            if (deviceService != null) {

                //如果部分失败直接返回
                IotResult result = deviceService.onWrite(serviceProp.getProperties());
                if (result.getResultCode() != IotResult.SUCCESS.getResultCode()) {
                    client.respondPropsSet(requestId, result);
                    return;
                }
            }
        }
        client.respondPropsSet(requestId, IotResult.SUCCESS);

    }

    /**
     * 属性查询回调，由SDK自动调用
     *
     * @param requestId 请求id
     * @param propsGet  属性查询请求
     */
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        List<ServiceProperty> serviceProperties = new ArrayList<>();

        //查询所有
        if (propsGet.getServiceId() == null) {

            for (String ss : services.keySet()) {
                IService deviceService = getService(ss);
                if (deviceService != null) {
                    Map properties = deviceService.onRead();
                    ServiceProperty serviceProperty = new ServiceProperty();
                    serviceProperty.setProperties(properties);
                    serviceProperty.setServiceId(ss);
                    serviceProperties.add(serviceProperty);
                }
            }
        } else {
            IService deviceService = getService(propsGet.getServiceId());

            if (deviceService != null) {
                Map properties = deviceService.onRead();
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setProperties(properties);
                serviceProperty.setServiceId(propsGet.getServiceId());
                serviceProperties.add(serviceProperty);

            }
        }

        client.respondPropsGet(requestId, serviceProperties);

    }

    /**
     * 事件回调，由SDK自动调用
     *
     * @param deviceEvents 设备事件
     */
    public void onEvent(DeviceEvents deviceEvents) {

        //子设备的
        if (deviceEvents.getDeviceId() != null && !deviceEvents.getDeviceId().equals(getDeviceId())) {
            return;
        }

        for (DeviceEvent event : deviceEvents.getServices()) {
            IService deviceService = getService(event.getServiceId());
            if (deviceService != null) {
                deviceService.onEvent(event);
            }
        }
    }

    /**
     * 消息回调，由SDK自动调用
     *
     * @param message 消息
     */
    public void onDeviceMessage(DeviceMessage message) {

    }

    /**
     * 获取OTA服务
     * @return OTAService
     */
    public OTAService getOtaService() {
        return otaService;
    }
}
