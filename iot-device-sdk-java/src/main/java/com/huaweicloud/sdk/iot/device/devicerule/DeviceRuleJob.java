package com.huaweicloud.sdk.iot.device.devicerule;

import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleAction;
import com.huaweicloud.sdk.iot.device.devicerule.model.TimeRange;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.List;

public class DeviceRuleJob implements Job {
    private static final Logger log = LogManager.getLogger(DeviceRuleJob.class);

    @Override
    public void execute(JobExecutionContext context) {
        try {
            final List<DeviceRuleAction> actionList = (List<DeviceRuleAction>) context.getMergedJobDataMap()
                .get("actionList");
            final DeviceRuleService deviceRuleService = (DeviceRuleService) context.getMergedJobDataMap()
                .get("deviceRuleService");
            final TimeRange timeRange = (TimeRange) context.getMergedJobDataMap().get("timeRange");
            if (deviceRuleService.checkTimeRange(timeRange)) {
                deviceRuleService.onRuleActionHandler(actionList);
            }
        } catch (Exception e) {
            log.warn("failed to execute DeviceRuleJob, exception={}", ExceptionUtil.getBriefStackTrace(e));
        }
    }
}
