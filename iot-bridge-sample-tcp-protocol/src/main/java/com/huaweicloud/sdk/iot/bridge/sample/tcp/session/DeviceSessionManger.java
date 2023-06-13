package com.huaweicloud.sdk.iot.bridge.sample.tcp.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeviceSessionManger {

    private static final DeviceSessionManger INSTANCE = new DeviceSessionManger();

    private ConcurrentMap<String, DeviceSession> sessions = new ConcurrentHashMap<>();

    public static DeviceSessionManger getInstance() {
        return INSTANCE;
    }

    public void createSession(String deviceId, DeviceSession session) {
        sessions.put(deviceId, session);
    }

    public DeviceSession getSession(String deviceId) {
        return sessions.get(deviceId);
    }

    public void deleteSession(String deviceId) {
        sessions.remove(deviceId);
    }
}
