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

    private Cache<String, CompletableFuture<Integer>> futureCache;

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
