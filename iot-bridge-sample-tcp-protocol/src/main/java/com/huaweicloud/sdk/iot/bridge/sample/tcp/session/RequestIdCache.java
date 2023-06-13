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
