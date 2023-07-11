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

package com.huaweicloud.sdk.iot.device.client.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 设备消息
 */
public class RawDeviceMessage {
    /**
     * 原始数据
     */
    private byte[] payload;

    private final Set<String> systemMessageKeys = new HashSet<>(
            Arrays.asList("name", "id", "content", "object_device_id"));

    public RawDeviceMessage() {
    }

    /**
     * 构造函数
     *
     * @param payload 消息内容
     */
    public RawDeviceMessage(byte[] payload) {
        this.payload = payload;
    }

    /**
     * 获取原始payload
     *
     * @return payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * 设置payload
     *
     * @param payload 消息内容
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String toUTF8String() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public DeviceMessage toDeviceMessage() {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        Map<String, Object> objectKV = JsonUtil.convertJsonStringToObject(toUTF8String(), typeRef);
        if (objectKV == null) {
            return null; // can't convert to system format
        }
        boolean isSystemFormat = objectKV.entrySet().stream()
                .allMatch(entry -> systemMessageKeys.contains(entry.getKey()));
        if (isSystemFormat) {
            return JsonUtil.convertMap2Object(objectKV, DeviceMessage.class);
        }
        return null;
    }
}
