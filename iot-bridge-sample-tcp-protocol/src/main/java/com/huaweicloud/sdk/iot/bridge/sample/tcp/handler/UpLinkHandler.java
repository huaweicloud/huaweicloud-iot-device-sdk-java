package com.huaweicloud.sdk.iot.bridge.sample.tcp.handler;

import com.huaweicloud.sdk.iot.bridge.sample.tcp.bridge.BridgeService;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.constants.Constants;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.BaseMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.CommonResponse;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.DeviceLoginMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.MsgHeader;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.DeviceSession;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.DeviceSessionManger;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.NettyUtils;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.RequestIdCache;
import com.huaweicloud.sdk.iot.device.client.listener.DefaultActionListenerImpl;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 设备上行数据处理类
 */
public class UpLinkHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LogManager.getLogger(UpLinkHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("receive msg={}", JsonUtil.convertObject2String(msg));
        if (!(msg instanceof BaseMessage)) {
            return;
        }
        upLinkDataHandle(ctx, (BaseMessage) msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String deviceId = NettyUtils.getDeviceId(ctx.channel());
        if (deviceId == null) {
            return;
        }
        DeviceSession deviceSession = DeviceSessionManger.getInstance().getSession(deviceId);
        if (deviceSession == null) {
            return;
        }

        // 调用网桥的logout接口，通知平台设备离线
        DefaultActionListenerImpl defaultLogoutActionListener = new DefaultActionListenerImpl("logout");
        BridgeService.getBridgeClient()
            .logoutAsync(deviceId, UUID.randomUUID().toString(), defaultLogoutActionListener);
        DeviceSessionManger.getInstance().deleteSession(deviceId);

        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("exceptionCaught. channelId={}, cause={}", ctx.channel().id(), cause.fillInStackTrace());
    }

    private void upLinkDataHandle(ChannelHandlerContext ctx, BaseMessage message) {

        switch (message.getMsgHeader().getMsgType()) {
            // DEVICE_LOGIN代表设备上线
            case Constants.MSG_TYPE_DEVICE_LOGIN:
                login(ctx.channel(), message);
                break;

            // 定时位置上报
            case Constants.MSG_TYPE_REPORT_LOCATION_INFO:
                reportProperties(ctx.channel(), message);
                break;

            // 位置上报周期的响应消息
            case Constants.MSG_TYPE_FREQUENCY_LOCATION_SET:
                responseCommand(message);
                break;
            default:
                break;
        }
    }

    private void login(Channel channel, BaseMessage message) {

        if (!(message instanceof DeviceLoginMessage)) {
            return;
        }

        String deviceId = message.getMsgHeader().getDeviceId();
        String secret = ((DeviceLoginMessage) message).getSecret();
        DeviceSession deviceSession = new DeviceSession();

        int resultCode = BridgeService.getBridgeClient().loginSync(deviceId, secret, 5000);

        // 登录成功保存会话信息
        if (resultCode == 0) {
            deviceSession.setDeviceId(deviceId);
            deviceSession.setChannel(channel);
            DeviceSessionManger.getInstance().createSession(deviceId, deviceSession);
            NettyUtils.setDeviceId(channel, deviceId);
        }

        // 构造登录响应的消息头
        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setDeviceId(deviceId);
        msgHeader.setFlowNo(message.getMsgHeader().getFlowNo());
        msgHeader.setDirect(Constants.DIRECT_CLOUD_RSP);
        msgHeader.setMsgType(Constants.MSG_TYPE_DEVICE_LOGIN);

        // 调用网桥login接口，向平台发起登录请求
        DefaultActionListenerImpl defaultLoginActionListener = new DefaultActionListenerImpl("login");
        BridgeService.getBridgeClient()
            .loginAsync(deviceId, secret, message.getMsgHeader().getFlowNo(),
                defaultLoginActionListener);
    }

    private void reportProperties(Channel channel, BaseMessage message) {
        String deviceId = message.getMsgHeader().getDeviceId();
        DeviceSession deviceSession = DeviceSessionManger.getInstance().getSession(deviceId);
        if (deviceSession == null) {
            log.warn("device={} is not login", deviceId);
            sendResponse(channel, message, 1);
            return;
        }

        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId("Location");
        serviceProperty.setProperties(
            JsonUtil.convertJsonStringToObject(JsonUtil.convertObject2String(message), Map.class));

        // 调用网桥reportProperties接口，上报设备属性数据
        BridgeService.getBridgeClient()
            .reportProperties(deviceId, Collections.singletonList(serviceProperty), new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    sendResponse(channel, message, 0);
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.warn("device={} reportProperties failed: {}", deviceId, ExceptionUtil.getBriefStackTrace(var2));
                    sendResponse(channel, message, 1);
                }
            });
    }

    private void responseCommand(BaseMessage message) {
        String deviceId = message.getMsgHeader().getDeviceId();
        DeviceSession deviceSession = DeviceSessionManger.getInstance().getSession(deviceId);
        if (deviceSession == null) {
            log.warn("device={} is not login", deviceId);
            return;
        }

        // 获取平台的requestId
        String requestId = RequestIdCache.getInstance().removeRequestId(deviceId, message.getMsgHeader().getFlowNo());
        if (requestId == null) {
            log.warn("device={} get requestId failed", deviceId);
            return;
        }

        if (!(message instanceof CommonResponse)) {
            log.warn("device={} invalid message", deviceId);
            return;
        }

        // 调用网桥接口返回命令响应
        CommonResponse response = (CommonResponse) message;
        BridgeService.getBridgeClient().respondCommand(deviceId, requestId, new CommandRsp(response.getResultCode()));

    }

    private void sendResponse(Channel channel, BaseMessage message, int resultCode) {
        CommonResponse commonResponse = new CommonResponse();
        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setDeviceId(message.getMsgHeader().getDeviceId());
        msgHeader.setFlowNo(message.getMsgHeader().getFlowNo());
        msgHeader.setMsgType(message.getMsgHeader().getMsgType());
        msgHeader.setDirect(Constants.DIRECT_CLOUD_RSP);
        commonResponse.setMsgHeader(msgHeader);
        commonResponse.setResultCode(resultCode);

        // 给设备返回登陆的响应消息
        channel.writeAndFlush(commonResponse);
    }

}