package com.huaweicloud.sdk.iot.bridge.sample.tcp.codec;

import com.huaweicloud.sdk.iot.bridge.sample.tcp.constants.Constants;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.BaseMessage;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.CommonResponse;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.DeviceLocationFrequencySet;
import com.huaweicloud.sdk.iot.bridge.sample.tcp.dto.MsgHeader;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 下行数据的消息编码，将具体对象转换为原始码流数据
 */
@ChannelHandler.Sharable
public class MessageEncoder extends ChannelOutboundHandlerAdapter {
    private static final Logger log = LogManager.getLogger(MessageEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        log.info("MessageEncoder msg={}", JsonUtil.convertObject2String(msg));
        BaseMessage baseMessage = (BaseMessage) msg;
        MsgHeader msgHeader = baseMessage.getMsgHeader();

        StringBuilder stringBuilder = new StringBuilder();
        encodeHeader(msgHeader, stringBuilder);

        // 根据消息类型编码消息
        switch (msgHeader.getMsgType()) {
            case Constants.MSG_TYPE_DEVICE_LOGIN:
            case Constants.MSG_TYPE_REPORT_LOCATION_INFO:
                stringBuilder.append(((CommonResponse) msg).getResultCode());
                break;
            case Constants.MSG_TYPE_FREQUENCY_LOCATION_SET:
                stringBuilder.append(((DeviceLocationFrequencySet) msg).getPeriod());
                break;
            default:
                log.warn("invalid msgType");
                return;
        }

        // 添加结束符
        stringBuilder.append(Constants.MESSAGE_END_DELIMITER);

        ctx.write(stringBuilder.toString(), promise);
    }

    private void encodeHeader(MsgHeader msgHeader, StringBuilder sb) {
        sb.append(Constants.MESSAGE_START_DELIMITER)
            .append(msgHeader.getDeviceId())
            .append(Constants.HEADER_PARS_DELIMITER)
            .append(msgHeader.getFlowNo())
            .append(Constants.HEADER_PARS_DELIMITER)
            .append(msgHeader.getMsgType())
            .append(Constants.HEADER_PARS_DELIMITER)
            .append(msgHeader.getDirect())
            .append(Constants.HEADER_PARS_DELIMITER);
    }
}
