package com.huaweicloud.sdk.iot.device.client.listener;

import com.huaweicloud.sdk.iot.device.transport.ActionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultActionListenerImpl implements ActionListener {
    private static final Logger log = LogManager.getLogger(DefaultActionListenerImpl.class);

    private final String actionType;

    public DefaultActionListenerImpl(String actionType) {
        this.actionType = actionType;
    }

    @Override
    public void onSuccess(Object context) {

    }

    @Override
    public void onFailure(Object context, Throwable var2) {
        log.error(actionType + "error", var2.getMessage());
    }
}
