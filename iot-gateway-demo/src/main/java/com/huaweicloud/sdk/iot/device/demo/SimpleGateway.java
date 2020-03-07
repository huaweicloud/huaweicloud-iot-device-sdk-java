package com.huaweicloud.sdk.iot.device.demo;


import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.listener.SubDeviceDownlinkListener;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceCommand;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.gateway.GatewayService;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;
import io.netty.channel.Channel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 此例用来演示使用网关来实现TCP协议设备接入。
 * SimpleGateway主要功能：
 * 1、继承SDK的GatewayService类实现子设备管理，从同步同步子设备信息保存到文件
 * 2、保存子设备的会话，子设备连接后生成一个Session保存到map
 * 3、子设备下行转发。调用setSubDeviceDownlinkListener处理子设备下行
 */
public class SimpleGateway {

    private static Logger log = Logger.getLogger(SimpleGateway.class);
    private Map<String, Session> nodeIdToSesseionMap; //保存设备标识码和session的映射
    private Map<String, Session> channelIdToSessionMap; //保存channelId和session的映射
    private GatewayService gatewayService;
    private IoTDevice ioTDevice;

    public SimpleGateway(IoTDevice ioTDevice) {

        this.nodeIdToSesseionMap = new ConcurrentHashMap<>();
        this.channelIdToSessionMap = new ConcurrentHashMap<>();
        this.ioTDevice = ioTDevice;
        this.gatewayService = new GatewayService(new SubDevicesFilePersistence(), ioTDevice);
        ioTDevice.addGatewayService(gatewayService);

        //处理网关自己的下行命令
        ioTDevice.getClient().setCommandListener(new CommandListener() {
            @Override
            public void onCommand(String requestId, String serviceId, String commandName, Map<String, Object> paras) {
                log.info("gateway onCommand:" + commandName);

                //todo 添加命令处理

                ioTDevice.getClient().respondCommand(requestId, new CommandRsp(0));
            }
        });

        //处理子设备下行
        ioTDevice.getClient().setSubDeviceDownlinkListener(new SubDeviceDownlinkListener() {
            @Override
            public void onCommand(String requestId, DeviceCommand command) {

                String nodeId = IotUtil.getNodeIdFromDeviceId(command.getDeviceId());
                if (nodeId == null) {
                    return;
                }

                Session session = nodeIdToSesseionMap.get(nodeId);
                if (session == null) {
                    log.error("session is null ,nodeId:" + nodeId);
                    ioTDevice.getClient().respondCommand(requestId, new CommandRsp(-1));
                    return;
                }

                //这里我们直接把对象转成string发给子设备，实际场景中可能需要进行一定的编解码转换
                session.getChannel().writeAndFlush(JsonUtil.convertObject2String(command));

                //为了简化处理，我们在这里直接回响应。更合理做法是在子设备处理完后再回响应
                ioTDevice.getClient().respondCommand(requestId, new CommandRsp(0));

                log.info("writeAndFlush ");
            }

            @Override
            public void onDeviceMessage(DeviceMessage message) {

                String nodeId = IotUtil.getNodeIdFromDeviceId(message.getDeviceId());
                if (nodeId == null) {
                    return;
                }

                Session session = nodeIdToSesseionMap.get(nodeId);
                if (session == null) {
                    log.error("session is null ,nodeId:" + nodeId);
                    return;
                }

                session.getChannel().writeAndFlush(message.getContent());
                log.info("writeAndFlush " + message.getContent());
            }

            @Override
            public void onPropertiesSet(String requestId, List<ServiceProperty> services, String subDeviceId) {

                String nodeId = IotUtil.getNodeIdFromDeviceId(subDeviceId);
                if (nodeId == null) {
                    return;
                }

                Session session = nodeIdToSesseionMap.get(nodeId);
                if (session == null) {
                    log.error("session is null ,nodeId:" + nodeId);
                    return;
                }

                //这里我们直接把对象转成string发给子设备，实际场景中可能需要进行一定的编解码转换
                session.getChannel().writeAndFlush(JsonUtil.convertObject2String(services));
                ioTDevice.getClient().respondPropsSet(requestId, IotResult.SUCCESS);

                log.info("writeAndFlush ");
            }

            @Override
            public void onPropertiesGet(String requestId, String serviceId, String subDeviceId) {

            }
        });

    }

    public IoTDevice getIoTDevice() {
        return ioTDevice;
    }


    public Session getSessionByChannel(String channelId) {
        return channelIdToSessionMap.get(channelId);
    }

    public void removeSession(String channelId) {
        Session session = channelIdToSessionMap.get(channelId);
        if (session == null) {
            return;
        }
        channelIdToSessionMap.remove(channelId);
        nodeIdToSesseionMap.remove(session.getNodeId());
        log.info("session removed " + session.toString());
    }

    public Session createSession(String nodeId, Channel channel) {

        //北向已经添加了此设备
        DeviceInfo subdev = gatewayService.getSubDeviceByNodeId(nodeId);
        if (subdev != null) {
            Session session = new Session();
            session.setChannel(channel);
            session.setNodeId(nodeId);
            session.setDeviceId(subdev.getDeviceId());

            nodeIdToSesseionMap.put(nodeId, session);
            channelIdToSessionMap.put(channel.id().asLongText(), session);
            log.info("create new session ok" + session.toString());
            return session;
        }

        log.info("not allowed : " + nodeId);
        return null;
    }

    public Session getSession(String nodeId) {
        return nodeIdToSesseionMap.get(nodeId);
    }


    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }

        IoTDevice ioTDevice = new IoTDevice("ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883",
                "5e06bfee334dd4f33759f5b3_demo", "secret");

        SimpleGateway simpleGateway = new SimpleGateway(ioTDevice);

        if (ioTDevice.init() != 0) {
            return;
        }

        Logger.getLogger("io.netty").setLevel(Level.INFO);
        new StringTcpServer(port, simpleGateway).run();

    }
}
