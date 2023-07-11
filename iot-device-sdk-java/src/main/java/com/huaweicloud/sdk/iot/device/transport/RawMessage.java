/*
 * Copyright (c) 2020-2023 Huawei Cloud Computing Technology Co., Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *    of conditions and the following disclaimer in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.huaweicloud.sdk.iot.device.transport;

import java.nio.charset.StandardCharsets;

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
     * qos,0或1，默认为1
     */
    private int qos;

    /**
     * 构造函数
     *
     * @param topic   消息topic
     * @param payload 消息体
     */
    public RawMessage(String topic, String payload) {
        this.topic = topic;
        this.payload = payload.getBytes();
        this.qos = 1;
    }

    /**
     * 构造函数
     *
     * @param topic   消息topic
     * @param payload 消息体
     */
    public RawMessage(String topic, byte[] payload) {
        this.topic = topic;
        this.payload = payload;
        this.qos = 1;
    }

    /**
     * 构造函数
     *
     * @param topic   消息topic
     * @param payload 消息体
     * @param qos     qos,0或1
     */
    public RawMessage(String topic, String payload, int qos) {
        this.qos = qos;
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

    /**
     * 查询qos
     *
     * @return qos
     */
    public int getQos() {
        return qos;
    }

    /**
     * 设置qos，0或1
     *
     * @param qos qos
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    @Override
    public String toString() {
        return new String(payload, StandardCharsets.UTF_8);
    }
}
