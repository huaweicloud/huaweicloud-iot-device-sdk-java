package com.huaweicloud.sdk.iot.device.client;


import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class IotRequest {

    private String requestId;
    private int timeout;
    private RawMessage rawMessage;
    private Object result = null;
    private boolean sync = true;
    private RequestListener listener;   //异步才有
    private Timer timer;

    private Logger log = Logger.getLogger(this.getClass());

    public IotRequest(RawMessage rawMessage, String requestId, int timeout) {

        if (timeout <= 0) {
            timeout = 10000;
        }
        this.timeout = timeout;
        this.rawMessage = rawMessage;
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RawMessage getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(RawMessage rawMessage) {
        this.rawMessage = rawMessage;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void runSync() {

        synchronized (this) {

            try {
                wait(timeout);
            } catch (InterruptedException e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }

            if (result == null) {
                result = IotResult.TIMEOUT;
                return;
            }

        }

    }

    public void runAync(RequestListener listener) {

        sync = false;
        this.listener = listener;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (result == null) {
                    result = IotResult.TIMEOUT;
                }
            }
        }, timeout);

    }

    public void onFinish(String iotResult) {

        synchronized (this) {
            this.result = iotResult;

            if (sync) {
                notifyAll();
            } else {
                if (listener != null) {
                    listener.onFinish(iotResult);
                }
            }

        }

    }

}
