package com.huaweicloud.sdk.iot.device.transport;

/**
 * 原始消息类
 */
public class RawMessage {

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息体
     */
    private byte[] payload;

    /**
     * 构造函数
     *
     * @param topic   消息topic
     * @param payload 消息体
     */
    public RawMessage(String topic, String payload) {
        this.topic = topic;
        this.payload = payload.getBytes();
    }


    /**
     * 查询topic
     *
     * @return 消息topic
     */
    public String getTopic() {
        return topic;
    }


    /**
     * 设置topic
     *
     * @param topic 消息topic
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 查询消息体
     *
     * @return 消息体
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * 设置消息体
     *
     * @param payload 消息体
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }


    @Override
    public String toString() {
        return new String(payload);
    }
}
