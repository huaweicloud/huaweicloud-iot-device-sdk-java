package com.huaweicloud.sdk.iot.device.devicerule;

import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleAction;

import java.util.List;

public interface ActionHandler {
    /**
     *  自定义规则触发器，用于客户自定义触发规则
     * @param actionList 端侧规则动作列表
     */
    void handleRuleAction(List<DeviceRuleAction> actionList);
}
