package com.huaweicloud.sdk.iot.device.demo;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.listener.DeviceMessageListener;
import com.huaweicloud.sdk.iot.device.client.listener.PropertyListener;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import io.netty.channel.Channel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 此例子用来演示如何使用协议网桥来实现TCP协议设备接入。网桥为每个TCP设备创建一个客户端（IotClient），使用设备的身份
 * 和平台进行通讯。本例子TCP server传输简单的字符串，并且首条消息会发送设备标识来鉴权。用户可以自行扩展StringTcpServer类
 * 来实现更复杂的TCP server。
 */
public class Bridge {

    private static Bridge instance;
    private static final Logger log = Logger.getLogger(Bridge.class);
    DeviceIdentityRegistry deviceIdentityRegistry;
    String serverUri;
    private Map<String, Session> deviceIdToSesseionMap;
    private Map<String, Session> channelIdToSessionMap;

    public Bridge(String serverUri, DeviceIdentityRegistry deviceIdentityRegistry) {
        this.serverUri = serverUri;

        if (deviceIdentityRegistry == null) {
            deviceIdentityRegistry = new DefaultDeviceIdentityRegistry();
        }
        this.deviceIdentityRegistry = deviceIdentityRegistry;
        deviceIdToSesseionMap = new ConcurrentHashMap<>();
        channelIdToSessionMap = new ConcurrentHashMap<>();
    }

    public static Bridge getInstance() {
        return instance;
    }

    public static void createBridge(String serverUri, DeviceIdentityRegistry deviceIdentityRegistry) {
        instance = new Bridge(serverUri, deviceIdentityRegistry);
    }

    public static void main(String[] args) throws Exception {

        //默认使用北京4的接入地址，其他region的用户请修改
        String serverUri = "ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883";

        int port = 8080;

        Logger.getLogger("io.netty").setLevel(Level.INFO);
        Bridge.createBridge(serverUri, null);

        new StringTcpServer(port).run();

    }

    public Session getSessionByChannel(String channelId) {
        return channelIdToSessionMap.get(channelId);
    }

    public void removeSession(String channelId) {
        Session session = channelIdToSessionMap.get(channelId);
        if (session != null) {
            session.getDeviceClient().close();
            deviceIdToSesseionMap.remove(session.getDeviceId());
            log.info("session removed " + session.toString());
        }
        channelIdToSessionMap.remove(channelId);

    }

    public int createSession(String nodeId, Channel channel) {

        //根据设备识别码获取设备标识信息
        DeviceIdentity deviceIdentity = deviceIdentityRegistry.getDeviceIdentity(nodeId);
        if (deviceIdentity == null) {
            log.error("deviceIdentity is null");
            return -1;
        }

        String deviceId = deviceIdentity.getDeviceId();
        IoTDevice ioTDevice = new IoTDevice(serverUri, deviceId, deviceIdentity.getSecret());
        int ret = ioTDevice.init();
        if (ret != 0) {
            return ret;
        }

        //创建会话
        Session session = new Session();
        session.setChannel(channel);
        session.setNodeId(nodeId);
        session.setDeviceId(deviceId);
        session.setDeviceClient(ioTDevice.getClient());

        //设置下行回调
        ioTDevice.getClient().setDeviceMessageListener(new DeviceMessageListener() {
            @Override
            public void onDeviceMessage(DeviceMessage deviceMessage) {

                //这里可以根据需要进行消息格式转换
                channel.writeAndFlush(deviceMessage.getContent());
            }
        });

        ioTDevice.getClient().setCommandListener(new CommandListener() {
            @Override
            public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {

                //这里可以根据需要进行消息格式转换
                channel.writeAndFlush(paras);

                //为了简化处理，我们在这里直接回命令响应。更合理做法是在设备处理完后再回响应
                ioTDevice.getClient().respondCommand(requestId, new CommandRsp(0));
            }
        });

        ioTDevice.getClient().setPropertyListener(new PropertyListener() {
            @Override
            public void onPropertiesSet(String requestId, List<ServiceProperty> services) {

                //这里可以根据需要进行消息格式转换
                channel.writeAndFlush(services);
                ioTDevice.getClient().respondPropsSet(requestId, IotResult.SUCCESS);
            }

            @Override
            public void onPropertiesGet(String requestId, String serviceId) {

                log.error("not supporte onSubdevPropertiesGet");
                ioTDevice.getClient().respondPropsSet(requestId, IotResult.FAIL);
            }
        });

        //保存会话
        deviceIdToSesseionMap.put(deviceId, session);
        channelIdToSessionMap.put(channel.id().asLongText(), session);

        log.info("create new session " + session.toString());
        return 0;

    }

}
