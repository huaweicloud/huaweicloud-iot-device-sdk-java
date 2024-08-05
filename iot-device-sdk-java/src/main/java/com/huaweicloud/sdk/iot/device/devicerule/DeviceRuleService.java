package com.huaweicloud.sdk.iot.device.devicerule;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.listener.CommandListener;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceInfo;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleAction;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleCommand;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleCondition;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleEventInfo;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleInfo;
import com.huaweicloud.sdk.iot.device.devicerule.model.TimeRange;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DeviceRuleService extends AbstractService {
    private static final Logger log = LogManager.getLogger(DeviceRuleService.class);

    private Map<String, DeviceRuleInfo> deviceRuleInfoMap = new ConcurrentHashMap<>();

    private Map<String, TimerRuleInstance> timerRuleInstanceMap = new ConcurrentHashMap<>();

    @Override
    public void onEvent(DeviceEvent deviceEvent) {
        final String serviceId = deviceEvent.getServiceId();
        final String eventType = deviceEvent.getEventType();
        if (!"$device_rule".equals(serviceId) && !"device_rule_config_response".equals(eventType)) {
            log.warn("serviceId={} or eventType={} is not match deviceRule!", serviceId, eventType);
            return;
        }
        final Map<String, Object> paras = deviceEvent.getParas();
        try {
            final DeviceRuleEventInfo deviceRuleEventInfo = JsonUtil.convertValue(paras,
                DeviceRuleEventInfo.class);
            deviceRuleEventInfo.getRuleInfos()
                .forEach(deviceRuleInfo -> {
                    final String ruleId = deviceRuleInfo.getRuleId();
                    final DeviceRuleInfo oldDeviceRuleInfo = deviceRuleInfoMap.get(ruleId);
                    if (oldDeviceRuleInfo == null
                        || oldDeviceRuleInfo.getRuleVersionInShadow() < deviceRuleInfo.getRuleVersionInShadow()) {
                        deviceRuleInfoMap.put(ruleId, deviceRuleInfo);
                        submitTimerRule(ruleId, deviceRuleInfo);
                    }
                });
            log.info("deviceRuleInfos is {}", deviceRuleInfoMap);
        } catch (Exception e) {
            log.warn("failed to execute onEvent, e={}", ExceptionUtil.getBriefStackTrace(e));
        }
    }

    private void setWrite(Map<String, Object> properties, List<String> ruleIds, List<String> delRuleIds) {
        properties.keySet().forEach(ruleId -> {
            final ObjectNode versionNode = JsonUtil.convertObject2ObjectNode(properties.get(ruleId));
            int version = versionNode.get("version").intValue();
            if (version == -1) {
                delRuleIds.add(ruleId);
                final DeviceRuleInfo deviceRuleInfo = deviceRuleInfoMap.get(ruleId);
                if (deviceRuleInfo != null) {
                    deviceRuleInfoMap.remove(ruleId);
                    final TimerRuleInstance timerRuleInstance = timerRuleInstanceMap.get(ruleId);
                    if (timerRuleInstance != null) {
                        timerRuleInstanceMap.remove(ruleId);
                        try {
                            timerRuleInstance.shutdown();
                        } catch (Exception e) {
                            log.warn("failed to shutdown timerRuleInstance, e={}",
                                    ExceptionUtil.getBriefStackTrace(e));
                        }
                    }
                }
            } else {
                ruleIds.add(ruleId);
            }
        });
    }

    @Override
    public IotResult onWrite(Map<String, Object> properties) {
        try {
            final List<String> ruleIds = new ArrayList<>();
            final List<String> delRuleIds = new ArrayList<>();

            setWrite(properties, ruleIds, delRuleIds);
            DeviceEvent deviceEvent = new DeviceEvent();
            deviceEvent.setServiceId("$device_rule");
            deviceEvent.setEventType("device_rule_config_request");
            deviceEvent.setEventTime(IotUtil.getTimeStamp());
            Map<String, Object> paras = new HashMap<>();
            paras.put("ruleIds", ruleIds);
            paras.put("delIds", delRuleIds);
            deviceEvent.setParas(paras);
            this.getIotDevice().getClient().reportEvent(deviceEvent, new ActionListener() {
                @Override
                public void onSuccess(Object context) {
                    log.info("report device_rule_config_request success");
                }

                @Override
                public void onFailure(Object context, Throwable var2) {
                    log.warn("report device_rule_config_request fail, ruleIds={}, delIds={}, ex={}", ruleIds,
                        delRuleIds, ExceptionUtil.getBriefStackTrace(var2));
                }
            });
            return IotResult.SUCCESS;
        } catch (Exception e) {
            log.warn("failed to execute onWrite, e={}", ExceptionUtil.getBriefStackTrace(e));
            return IotResult.FAIL;
        }
    }

    public void handleRule(List<ServiceProperty> properties) {
        deviceRuleInfoMap.keySet().forEach(ruleId -> {
            final DeviceRuleInfo deviceRuleInfo = deviceRuleInfoMap.get(ruleId);
            if (!"active".equals(deviceRuleInfo.getStatus())) {
                log.info("rule status={} is not active", deviceRuleInfo.getStatus());
                return;
            }
            // 判断是否在时间范围内
            if (!checkTimeRange(deviceRuleInfo.getTimeRange())) {
                log.warn("rule was not match the time!");
                return;
            }
            final List<DeviceRuleCondition> conditions = deviceRuleInfo.getConditions();
            final String logic = deviceRuleInfo.getLogic();
            if ("or".equals(logic)) {
                for (DeviceRuleCondition condition : conditions) {
                    final boolean flag = isConditionSatisfied(condition, properties);
                    if (flag) {
                        onRuleActionHandler(deviceRuleInfo.getActions());
                        return;
                    }
                }
            } else if ("and".equals(logic)) {
                boolean isSatisfied = true;
                for (DeviceRuleCondition condition : conditions) {
                    final boolean flag = isConditionSatisfied(condition, properties);
                    if (!flag) {
                        isSatisfied = false;
                        break;
                    }
                }
                if (isSatisfied) {
                    onRuleActionHandler(deviceRuleInfo.getActions());
                }
            } else {
                log.warn("rule logic is not match. logic: {}", logic);
            }
        });
    }

    public void onRuleActionHandler(List<DeviceRuleAction> actionList) {
        final ActionHandler actionHandler = getIotDevice().getClient().getActionHandler();
        if (actionHandler != null) {
            actionHandler.handleRuleAction(actionList);
            return;
        }
        if (actionList == null || actionList.isEmpty()) {
            log.warn("rule action list is empty!");
            return;
        }
        for (DeviceRuleAction action : actionList) {
            if (!getIotDevice().getDeviceId().equals(action.getDeviceId())) {
                log.warn("action device is not match: target: {}, action: {}", getIotDevice().getDeviceId(),
                    action.getDeviceId());
                continue;
            }
            handleAction(action);
        }
    }

    private void handleAction(DeviceRuleAction action) {
        final DeviceRuleCommand command = action.getCommand();
        if (command == null) {
            log.warn("rule command is null!");
            return;
        }
        final CommandListener commandListener = getIotDevice().getClient().getCommandListener();
        if (commandListener == null) {
            log.warn("command listener was not config for rules!");
            return;
        }
        commandListener.onCommand(UUID.randomUUID().toString(), command.getServiceId(), command.getCommandName(),
            command.getCommandBody());
    }

    private boolean isConditionSatisfied(DeviceRuleCondition condition, List<ServiceProperty> properties) {
        try {
            if (!"DEVICE_DATA".equals(condition.getType())) {
                return false;
            }

            final DeviceInfo deviceInfo = condition.getDeviceInfo();
            final String[] pathArray = deviceInfo.getPath().split("/");
            if (pathArray.length != 2) {
                log.warn("rule condition path is invalid. path: {}", deviceInfo.getPath());
                return false;
            }

            final String serviceId = pathArray[0];
            final String property = pathArray[1];
            return operation(condition.getValue(), condition.getInValues(), serviceId, property, properties,
                condition.getOperator());
        } catch (Exception e) {
            log.warn("failed to execute isConditionSatisfied, e={}", ExceptionUtil.getBriefStackTrace(e));
        }
        return false;
    }

    private boolean operation(String value, List<String> inValues, String serviceId, String property,
        List<ServiceProperty> properties, String operator) {
        if ("between".equals(operator)) {
            final String[] valueArray = value.split(",");
            if (valueArray.length != 2) {
                log.warn("rule condition value is invalid. value: {}", value);
                return false;
            }

            for (ServiceProperty serviceProperty : properties) {
                final Object propertyValue = String.valueOf(serviceProperty.getProperties().get(property));
                if (serviceId.equals(serviceProperty.getServiceId()) && valueCompare(propertyValue, valueArray[0],
                    ">=") && valueCompare(propertyValue, valueArray[1],
                    "<=")) {
                    return true;
                }
            }
            return false;
        }
        if ("in".equals(operator)) {
            for (ServiceProperty serviceProperty : properties) {
                if (serviceId.equals(serviceProperty.getServiceId()) && isInValues(
                    serviceProperty.getProperties().get(property), inValues)) {
                    return true;
                }
            }
            return false;
        }

        for (ServiceProperty serviceProperty : properties) {
            if (serviceId.equals(serviceProperty.getServiceId()) && valueCompare(
                serviceProperty.getProperties().get(property), value, operator)) {
                log.info("match condition for service. serviceId: {}, value: {}", serviceId, value);
                return true;
            }
        }

        return false;
    }

    private boolean isInValues(Object left, List<String> inValues) {
        return inValues.contains(String.valueOf(left));
    }

    private boolean valueCompare(Object left, Object right, String operator) {
        if (!Objects.isNull(left) && !Objects.isNull(right)) {
            String l = String.valueOf(left);
            String r = String.valueOf(right);

            try {
                switch (operator) {
                    case "=":
                        return equals(l, r);
                    case ">":
                        return Float.valueOf(l) > Float.valueOf(r);
                    case ">=":
                        return Float.valueOf(l) >= Float.valueOf(r);
                    case "<":
                        return Float.valueOf(l) < Float.valueOf(r);
                    case "<=":
                        return Float.valueOf(l) <= Float.valueOf(r);
                    default:
                        log.warn("operator id other. operator = {}", operator);
                }

            } catch (NumberFormatException exception) {
                log.warn("String trans to Float failed! left is {}, right is {}", left, right);
            }
        }
        log.warn("compare result is false, left is {}, right is {}, operator is {}", left, right, operator);
        return false;
    }

    private boolean equals(String left, String right) {
        if (left.equals(right)) {
            return true;
        } else {
            try {
                return Float.valueOf(left).equals(Float.valueOf(right));
            } catch (Exception e) {
                log.warn("String trans to Float failed! left is {}, right is {}", left, right);
            }
            return false;
        }
    }

    public boolean checkTimeRange(TimeRange timeRange) {
        if (timeRange == null) {
            return true;
        }
        String beginTime = timeRange.getStartTime();
        String endTime = timeRange.getEndTime();
        String weekStr = timeRange.getDaysOfWeek();
        if (isEmpty(beginTime) || isEmpty(endTime) || isEmpty(weekStr)) {
            return false;
        }
        final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"));
        int nowWeek = (now.getDayOfWeek().getValue() + 1) % 7;
        log.info("nowWeek={}", nowWeek);
        String[] weekStrList = weekStr.split(",");
        final List<Integer> weekList = Arrays.asList(weekStrList)
            .stream()
            .map(week -> Integer.parseInt(week))
            .collect(Collectors.toList());
        // 开始结束时间分割成[Hour, Minute]
        final String[] beginTimeList = beginTime.split(":");
        int beginHour = Integer.parseInt(beginTimeList[0]);
        int beginMinute = Integer.parseInt(beginTimeList[1]);
        final String[] endTimeList = endTime.split(":");
        int endHour = Integer.parseInt(endTimeList[0]);
        int endMinute = Integer.parseInt(endTimeList[1]);
        int nowHour = now.getHour();
        int nowMinute = now.getMinute();
        int beginInMinute = beginHour * 60 + beginMinute;
        int nowInMinute = nowHour * 60 + nowMinute;
        int endInMinute = endHour * 60 + endMinute;

        // 8:00 -9:00形式
        if (beginInMinute < endInMinute) {
            return beginInMinute <= nowInMinute && nowInMinute <= endInMinute && weekList.contains(nowWeek);
        }
        // 23:00 -01:00形式， 处于23:00-00:00之间的形式
        if ((beginInMinute <= nowInMinute) && (nowInMinute <= 24 * 60 + 60) && weekList.contains(nowWeek)) {
            return true;
        } else if (nowInMinute <= endInMinute) {
            nowWeek = nowWeek - 1;
            if (nowWeek == 0) {
                nowWeek = 7;
            }
            return weekList.contains(nowWeek);
        }

        return false;
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private void submitTimerRule() throws Exception {

        for (Map.Entry<String, DeviceRuleInfo> entry : deviceRuleInfoMap.entrySet()) {
            final DeviceRuleInfo deviceRuleInfo = entry.getValue();
            String key = entry.getKey();
            final List<DeviceRuleCondition> conditions = deviceRuleInfo.getConditions();
            boolean isTimerRule = false;
            for (DeviceRuleCondition condition : conditions) {
                if ("DAILY_TIMER".equals(condition.getType()) || "SIMPLE_TIMER".equals(condition.getType())) {
                    isTimerRule = true;
                    break;
                }
            }
            if (isTimerRule && conditions.size() > 1 && "and".equals(deviceRuleInfo.getLogic())) {
                log.warn("multy timer rule only support or logic. ruleId: {}", key);
                continue;
            }
            if (timerRuleInstanceMap.get(key) != null) {
                timerRuleInstanceMap.get(key).shutdown();
                timerRuleInstanceMap.remove(key);
            }
            if (!"active".equals(deviceRuleInfo.getStatus())) {
                log.info("rule status={} is not active", deviceRuleInfo.getStatus());
                return;
            }
            TimerRuleInstance timerRuleInstance = new TimerRuleInstance(this);
            timerRuleInstance.submitRule(deviceRuleInfo);
            timerRuleInstance.start();
            timerRuleInstanceMap.put(key, timerRuleInstance);
        }
    }

    private void submitTimerRule(String ruleId, DeviceRuleInfo deviceRuleInfo) {
        try {
            final List<DeviceRuleCondition> conditions = deviceRuleInfo.getConditions();
            boolean isTimerRule = false;
            for (DeviceRuleCondition condition : conditions) {
                if ("DAILY_TIMER".equals(condition.getType()) || "SIMPLE_TIMER".equals(condition.getType())) {
                    isTimerRule = true;
                    break;
                }
            }
            if (isTimerRule && conditions.size() > 1 && "and".equals(deviceRuleInfo.getLogic())) {
                log.warn("multy timer rule only support or logic. ruleId: {}", ruleId);
                return;
            }
            if (timerRuleInstanceMap.get(ruleId) != null) {
                timerRuleInstanceMap.get(ruleId).shutdown();
                timerRuleInstanceMap.remove(ruleId);
            }
            if (!"active".equals(deviceRuleInfo.getStatus())) {
                log.info("rule status={} is not active", deviceRuleInfo.getStatus());
                return;
            }
            TimerRuleInstance timerRuleInstance = new TimerRuleInstance(this);
            timerRuleInstance.submitRule(deviceRuleInfo);
            timerRuleInstance.start();
            timerRuleInstanceMap.put(ruleId, timerRuleInstance);
        } catch (Exception e) {
            log.warn("submitTimerRule error,ex={}", ExceptionUtil.getBriefStackTrace(e));
        }
    }
}
