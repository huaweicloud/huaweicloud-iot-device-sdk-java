package com.huaweicloud.sdk.iot.device.client;

import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 请求管理器
 */
public class RequestManager {
   
    private DeviceClient iotClient;
    private ConcurrentMap<String, IotRequest> pendingRequests = new ConcurrentHashMap<>();
    private static final Logger log = LogManager.getLogger(RequestManager.class);

    /**
     * 构造函数
     *
     * @param client 客户端
     */
    public RequestManager(DeviceClient client) {
        this.iotClient = client;
    }

    /**
     * 执行同步请求
     *
     * @param iotRequest 请求参数
     * @return 请求执行结果
     */
    public Object executeSyncRequest(IotRequest iotRequest) {

        RawMessage rawMessage = iotRequest.getRawMessage();
        iotClient.publishRawMessage(rawMessage, null);
        pendingRequests.put(iotRequest.getRequestId(), iotRequest);
        iotRequest.runSync();
        return iotRequest.getResult();
    }

    /**
     * 执行异步请求
     *
     * @param iotRequest 请求参数
     * @param listener   请求监听器，用于接收请求完成通知
     */
    public void executeAsyncRequest(IotRequest iotRequest, RequestListener listener) {

        RawMessage rawMessage = iotRequest.getRawMessage();
        iotClient.publishRawMessage(rawMessage, null);
        pendingRequests.put(iotRequest.getRequestId(), iotRequest);
        iotRequest.runAsync(listener);
    }

    /**
     * 请求响应回调，由sdk自动调用
     *
     * @param message 响应消息
     */
    public void onRequestResponse(RawMessage message) {
        String requestId = IotUtil.getRequestId(message.getTopic());
        IotRequest request = pendingRequests.remove(requestId);
        if (request == null) {
            log.error("request is null, requestId: " + requestId);
            return;
        }

        request.onFinish(message.toString());
    }
}
