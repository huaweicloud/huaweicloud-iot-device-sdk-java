package com.huaweicloud.sdk.iot.bridge.sample.tcp.handler;

import com.huaweicloud.bridge.sdk.listener.BridgeCommandListener;
import com.huaweicloud.bridge.sdk.listener.BridgeDeviceDisConnListener;
import com.huaweicloud.bridge.sdk.listener.BridgeDeviceMessageListener;
import com.huaweicloud.bridge.sdk.request.BridgeCommand;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.constants.Constants;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.DeviceLocationFrequencySet;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.MsgHeader;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.DeviceSessionManger;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.RequestIdCache;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.session.DeviceSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * 平台下行数据处理类
 */
public class DownLinkHandler implements BridgeDeviceMessageListener, BridgeCommandListener,
    BridgeDeviceDisConnListener {
    private static final Logger log = LogManager.getLogger(DownLinkHandler.class);

    // 网桥sample中处理的是属性和命令，消息透传用户可自行实现
    @Override
    public void onDeviceMessage(String deviceId, DeviceMessage deviceMessage) {

    }

    @Override
    public void onCommand(String deviceId, String requestId, BridgeCommand bridgeCommand) {
        log.info("onCommand deviceId={}, requestId={}, bridgeCommand={}", deviceId, requestId, bridgeCommand);
        DeviceSession session = DeviceSessionManger.getInstance().getSession(deviceId);
        if (session == null) {
            log.warn("device={} session is null", deviceId);
            return;
        }

        // 设置位置上报的周期
        if (Constants.MSG_TYPE_FREQUENCY_LOCATION_SET.equals(bridgeCommand.getCommand().getCommandName())) {
            processLocationSetCommand(session, requestId, bridgeCommand);
        }
    }

    private void processLocationSetCommand(DeviceSession session, String requestId, BridgeCommand bridgeCommand) {
        int flowNo = session.getAndUpdateSeqId();

        // 构造消息头
        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setDeviceId(session.getDeviceId());
        msgHeader.setFlowNo(String.valueOf(flowNo));
        msgHeader.setDirect(Constants.DIRECT_CLOUD_REQ);
        msgHeader.setMsgType(bridgeCommand.getCommand().getCommandName());

        // 根据参数内容构造消息体
        Map<String, Object> paras = bridgeCommand.getCommand().getParas();
        DeviceLocationFrequencySet locationFrequencySet = new DeviceLocationFrequencySet();
        locationFrequencySet.setPeriod((Integer) paras.get("period"));
        locationFrequencySet.setMsgHeader(msgHeader);

        // 发下消息到设备
        session.getChannel().writeAndFlush(locationFrequencySet);

        // 记录平台requestId和设备流水号的关联关系，用于关联命令的响应
        RequestIdCache.getInstance().setRequestId(session.getDeviceId(), String.valueOf(flowNo), requestId);
    }

    @Override
    public void onDisConnect(String deviceId) {
        // 关闭session
        DeviceSession session = DeviceSessionManger.getInstance().getSession(deviceId);
        Optional.of(session).ifPresent(s -> s.getChannel().close());

        // 删除会话
        DeviceSessionManger.getInstance().deleteSession(deviceId);
    }

}
