package com.huaweicloud.sdk.iot.bridge.sample.tcp.codec;

import com.huaweicloud.sdk.iot.bridge.sample.tcp.constants.Constants;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.BaseMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.CommonResponse;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.DeviceLocationMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.DeviceLoginMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.MsgHeader;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 上行数据的消息解码，将原始码流转换为具体对象
 */
@ChannelHandler.Sharable
public class MessageDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger log = LogManager.getLogger(MessageDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("MessageDecoder msg={}", msg);
        if (!checkInComingMsg(msg)) {
            return;
        }
        int startIndex = ((String) msg).indexOf(Constants.MESSAGE_START_DELIMITER);
        if (startIndex < 0) {
            return;
        }

        BaseMessage message = decodeMessage(((String) msg).substring(startIndex + 1));
        if (message == null) {
            log.warn("decode message failed");
            return;
        }
        ctx.fireChannelRead(message);
    }

    private boolean checkInComingMsg(Object msg) {
        return msg instanceof String && ((String) msg).length() != 0;
    }

    private BaseMessage decodeMessage(String message) {
        MsgHeader header = decodeHeader(message);
        if (header == null) {
            return null;
        }
        BaseMessage baseMessage = decodeBody(header, message.substring(message.lastIndexOf(",") + 1));
        if (baseMessage == null) {
            return null;
        }
        baseMessage.setMsgHeader(header);
        return baseMessage;
    }

    private MsgHeader decodeHeader(String message) {
        String[] splits = message.split(Constants.HEADER_PARS_DELIMITER);
        if (splits.length <= 4) {
            return null;
        }

        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setDeviceId(splits[0]);
        msgHeader.setFlowNo(splits[1]);
        msgHeader.setMsgType(splits[2]);
        msgHeader.setDirect(Integer.parseInt(splits[3]));
        return msgHeader;
    }

    private BaseMessage decodeBody(MsgHeader header, String body) {
        switch (header.getMsgType()) {
            case Constants.MSG_TYPE_DEVICE_LOGIN:
                return decodeLoginMessage(body);

            case Constants.MSG_TYPE_REPORT_LOCATION_INFO:
                return decodeLocationMessage(body);

            case Constants.MSG_TYPE_FREQUENCY_LOCATION_SET:
                return decodeLocationSetMessage(body);

            default:
                log.warn("invalid msgType");
                return null;
        }
    }

    private BaseMessage decodeLoginMessage(String body) {
        DeviceLoginMessage loginMessage = new DeviceLoginMessage();
        loginMessage.setSecret(body);
        return loginMessage;
    }

    private BaseMessage decodeLocationMessage(String body) {
        String[] splits = body.split(Constants.BODY_PARS_DELIMITER);
        DeviceLocationMessage deviceLocationMessage = new DeviceLocationMessage();
        deviceLocationMessage.setLongitude(splits[0]);
        deviceLocationMessage.setLatitude(splits[1]);
        return deviceLocationMessage;
    }

    private BaseMessage decodeLocationSetMessage(String body) {
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setResultCode(Integer.parseInt(body));
        return commonResponse;
    }

}