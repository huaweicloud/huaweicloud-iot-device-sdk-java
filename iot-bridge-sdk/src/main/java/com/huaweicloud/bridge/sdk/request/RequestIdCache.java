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

package com.huaweicloud.bridge.sdk.request;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * RequestId的缓存实现样例，将登录的requestId同future关联。
 */

public class RequestIdCache {
    private static final Logger log = LogManager.getLogger(RequestIdCache.class);

    private final Cache<String, CompletableFuture<Integer>> futureCache;

    private static final int INITIAL_CAPACITY = 200;

    private static final int MAX_CAPACITY = 2000;

    public RequestIdCache() {
        futureCache = CacheBuilder.newBuilder().initialCapacity(INITIAL_CAPACITY).maximumSize(MAX_CAPACITY)
            .expireAfterWrite(3, TimeUnit.MINUTES).build();
    }

    public void setRequestId2Cache(String requestId, CompletableFuture<Integer> future) {
        futureCache.put(requestId, future);
    }

    public void invalidateCache(String key) {
        futureCache.invalidate(key);
    }

    public CompletableFuture<Integer> getFuture(String requestId) {
        try {
            CompletableFuture<Integer> value = futureCache.getIfPresent(requestId);
            invalidateCache(requestId);
            return value;
        } catch (Exception e) {
            log.warn("getRequestId error : {} for key: {}", e, requestId);
            return null;
        }
    }

}
