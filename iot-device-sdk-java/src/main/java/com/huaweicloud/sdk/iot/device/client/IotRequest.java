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

package com.huaweicloud.sdk.iot.device.client;

import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IotRequest {
    private static final Logger log = LogManager.getLogger(IotRequest.class);

    private String requestId;

    // 单位：毫秒
    private int timeout;

    private RawMessage rawMessage;

    private Object result = null;

    private boolean sync = true;

    /**
     * 异步请求才有
     */
    private RequestListener listener;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public IotRequest(RawMessage rawMessage, String requestId, int timeoutOfMilliSeconds) {

        if (timeoutOfMilliSeconds <= 0) {
            timeoutOfMilliSeconds = 10000;
        }
        this.timeout = timeoutOfMilliSeconds;
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
                while (timeout != 0) {
                    wait(timeout);
                    timeout = 0;
                }
            } catch (InterruptedException e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
            }

            if (result == null) {
                result = IotResult.TIMEOUT;
            }

        }

    }

    public void runAsync(RequestListener listener) {

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
