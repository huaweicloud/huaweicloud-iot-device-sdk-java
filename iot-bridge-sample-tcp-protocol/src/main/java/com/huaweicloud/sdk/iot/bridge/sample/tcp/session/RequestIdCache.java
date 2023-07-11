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

package com.huaweicloud.sdk.iot.bridge.sample.tcp.session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class RequestIdCache {
    private static final Logger log = LogManager.getLogger(RequestIdCache.class);

    private static final String SEPARATOR = ":";

    private static final RequestIdCache INSTANCE = new RequestIdCache();

    private final Cache<String, String> cache = CacheBuilder.newBuilder()
        .initialCapacity(2000)
        .maximumSize(20000)
        .expireAfterWrite(3, TimeUnit.MINUTES)
        .build();

    public static RequestIdCache getInstance() {
        return INSTANCE;
    }

    public void setRequestId(String deviceId, String flowNo, String requestId) {
        cache.put(getKey(deviceId, flowNo), requestId);
    }

    public String removeRequestId(String deviceId, String flowNo) {
        String key = getKey(deviceId, flowNo);
        try {
            String value = cache.getIfPresent(key);
            cache.invalidate(key);
            return value;
        } catch (Exception e) {
            log.warn("getRequestId error : {} for key: {}", ExceptionUtil.getBriefStackTrace(e), key);
            return null;
        }
    }

    private String getKey(String deviceId, String flowNo) {
        return deviceId + SEPARATOR + flowNo;
    }
}
