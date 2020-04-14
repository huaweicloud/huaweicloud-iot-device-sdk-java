package com.huaweicloud.sdk.iot.device.client;

import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IotRequest {
    private static final Logger log = Logger.getLogger(IotRequest.class);
    private String requestId;
    private int timeout;
    private RawMessage rawMessage;
    private Object result = null;
    private boolean sync = true;
    /**
     * 异步请求才有
     */
    private RequestListener listener;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();;

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
            }

        }

    }

    public void runAync(RequestListener listener) {

        sync = false;
        this.listener = listener;
        executor.scheduleWithFixedDelay(() -> {
            if (result == null) {
                result = IotResult.TIMEOUT;
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);
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
