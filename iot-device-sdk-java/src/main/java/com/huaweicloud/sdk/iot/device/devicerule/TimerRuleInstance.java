package com.huaweicloud.sdk.iot.device.devicerule;

import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleCondition;
import com.huaweicloud.sdk.iot.device.devicerule.model.DeviceRuleInfo;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TimerRuleInstance {
    private static final Logger log = LogManager.getLogger(TimerRuleInstance.class);

    private Scheduler scheduler;

    private DeviceRuleService deviceRuleService;

    TimerRuleInstance(DeviceRuleService deviceRuleService) throws SchedulerException {
        this.deviceRuleService = deviceRuleService;
        scheduler = new StdSchedulerFactory().getScheduler();
    }

    public void submitRule(DeviceRuleInfo ruleInfo) throws Exception {
        final List<DeviceRuleCondition> conditions = ruleInfo.getConditions();
        for (DeviceRuleCondition condition : conditions) {
            final String type = condition.getType();
            if ("DAILY_TIMER".equals(type)) {
                final String executeTime = condition.getTime();
                final String daysOfWeek = condition.getDaysOfWeek();
                if (IotUtil.isStringEmpty(executeTime) && IotUtil.isStringEmpty(daysOfWeek)) {
                    log.warn("time or days of week is empty, time={}, daysOfWeek={}", executeTime, daysOfWeek);
                    return;
                }
                final String[] timeList = executeTime.split(":");
                if (timeList.length != 2) {
                    log.warn("time format is invalid. time={}", executeTime);
                    return;
                }
                final String[] daysList = daysOfWeek.split(",");
                final Integer[] intTimes = IotUtil.strArrayToInteger(timeList);
                final Integer[] intDaysList = IotUtil.strArrayToInteger(daysList);
                final JobDetail jobDetail = getJobDetail(ruleInfo);
                Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(
                        CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek(intTimes[0], intTimes[1], intDaysList)
                            .inTimeZone(TimeZone.getTimeZone("UTC")))
                    .build();
                scheduler.scheduleJob(jobDetail, trigger);
            } else if ("SIMPLE_TIMER".equals(type)) {
                final int repeatInterval = condition.getRepeatInterval();
                final int repeatCount = condition.getRepeatCount();
                final String startTime = condition.getStartTime();
                final JobDetail jobDetail = getJobDetail(ruleInfo);
                Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(repeatCount)
                        .withIntervalInSeconds(repeatInterval))
                    .startAt(strToDate(startTime))
                    .build();
                scheduler.scheduleJob(jobDetail, trigger);
            }
        }
    }

    private Date strToDate(String strTime) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(strTime);
    }

    private JobDetail getJobDetail(DeviceRuleInfo ruleInfo) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("actionList", ruleInfo.getActions());
        jobDataMap.put("deviceRuleService", deviceRuleService);
        jobDataMap.put("timeRange", ruleInfo.getTimeRange());
        JobDetail jobDetail = JobBuilder.newJob(DeviceRuleJob.class).setJobData(jobDataMap).build();
        return jobDetail;
    }

    public void start() throws SchedulerException {
        if (scheduler != null) {
            scheduler.start();
        }
    }

    public void shutdown() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
