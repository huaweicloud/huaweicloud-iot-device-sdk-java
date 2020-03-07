package com.huaweicloud.sdk.iot.device.service;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceCommand;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 抽象服务类，提供了属性自动读写和命令调用能力，用户可以继承此类，根据物模型定义自己的服务
 */
@JsonFilter("AbstractService")
public abstract class AbstractService implements IService {

    private Logger log = Logger.getLogger(AbstractService.class);

    private IoTDevice iotDevice;
    private Map<String, Method> commands = new HashMap<>();
    private Map<String, Field> writeableFields = new HashMap<>();
    private Map<String, FieldPair> readableFields = new HashMap<>();
    private int reportInterval = 0;
    private Timer timer;
    private String serviceId;

    private static class FieldPair {
        public String propertyName;
        public Field field;

        public FieldPair(String propertyName, Field field) {
            this.propertyName = propertyName;
            this.field = field;
        }
    }

    public AbstractService() {

        for (Field field : this.getClass().getDeclaredFields()) {

            Property property = field.getAnnotation(Property.class);
            if (property == null) {
                continue;
            }

            String name = property.name();
            if (name.isEmpty()) {
                name = field.getName();
            }
            if (property.writeable()) {
                writeableFields.put(name, field);
            }

            //这里key是字段名,pair里保存属性名
            readableFields.put(field.getName(), new FieldPair(name, field));
        }

        for (Method method : this.getClass().getDeclaredMethods()) {
            com.huaweicloud.sdk.iot.device.service.DeviceCommand deviceCommand = method.getAnnotation(com.huaweicloud.sdk.iot.device.service.DeviceCommand.class);
            if (deviceCommand == null) {
                continue;
            }
            String name = deviceCommand.name();
            if (name.isEmpty()) {
                name = method.getName();
            }
            commands.put(name, method);
        }

    }

    private Object getFiledValue(String fieldName) {

        Field field = readableFields.get(fieldName).field;
        if (field == null) {
            log.error("field is null: " + fieldName);
            return null;
        }
        String getter = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method method;

        try {
            method = this.getClass().getDeclaredMethod(getter);
        } catch (NoSuchMethodException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return null;
        }

        if (method == null) {
            log.error("method is null: " + getter);
            return null;
        }

        try {
            Object value = method.invoke(this);
            return value;
        } catch (IllegalAccessException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        } catch (InvocationTargetException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
        }

        return null;

    }


    /**
     * 读属性回调
     *
     * @param fields 指定读取的字段名，不指定则读取全部可读字段
     * @return 属性值
     */
    @Override
    public Map<String, Object> onRead(String... fields) {

        Map<String, Object> ret = new HashMap<>();

        //读取指定的字段
        if (fields.length > 0) {
            for (String fieldName : fields) {

                if (readableFields.get(fieldName) == null) {
                    log.error("field is not readable:" + fieldName);
                    continue;
                }

                Object value = getFiledValue(fieldName);
                if (value != null) {
                    ret.put(readableFields.get(fieldName).propertyName, value);
                }
            }

            return ret;
        }

        //读取全部字段
        for (Map.Entry<String, FieldPair> entry : readableFields.entrySet()) {
            Object value = getFiledValue(entry.getKey());
            if (value != null) {
                ret.put(entry.getValue().propertyName, value);
            }
        }
        return ret;

    }


    /**
     * 写属性。收到平台下发的写属性操作时此接口被自动调用。
     * 如果用户希望在写属性时增加额外处理，可以重写此接口
     *
     * @param properties 平台期望属性的值
     * @return 操作结果
     */
    @Override
    public IotResult onWrite(Map<String, Object> properties) {


        List<String> changedProps = new ArrayList<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {

            Field field = writeableFields.get(entry.getKey());
            if (field == null) {
                log.error("field not found or not writeable " + entry.getKey());
                return new IotResult(-1, "field not found or not writeable " + entry.getKey());

            }

            Object value = entry.getValue();
            String setter = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            Method method = null;

            try {
                method = this.getClass().getDeclaredMethod(setter, field.getType());
            } catch (NoSuchMethodException e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));

            }

            if (method == null) {
                log.error("method not found： " + setter);
                return new IotResult(-1, "method not found： " + setter);
            }

            try {
                method.invoke(this, value);
                log.info("write property ok:" + entry.getKey());
                changedProps.add(field.getName());
            } catch (Exception e) {
                log.error(ExceptionUtil.getBriefStackTrace(e));
                return new IotResult(-1, e.getMessage());
            }
        }

        //上报变化的属性
        if (changedProps.size() > 0) {
            firePropertiesChanged(changedProps.toArray(new String[changedProps.size()]));
        }

        return IotResult.SUCCESS;
    }

    /**
     * 事件处理。收到平台下发的事件时此接口被自动调用。默认为空实现
     *
     * @param deviceEvent 服务事件
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {
        log.info("onEvent no op");
    }

    /**
     * 通知服务属性变化
     *
     * @param properties 变化的属性，不指定默认读取全部可读属性
     */
    public void firePropertiesChanged(String... properties) {
        iotDevice.firePropertiesChanged(getServiceId(), properties);
    }

    /**
     * 执行设备命令。收到平台下发的命令时此接口被自动调用
     *
     * @param command 命令请求
     * @return 命令响应
     */
    @Override
    public CommandRsp onCommand(DeviceCommand command) {

        Method method = commands.get(command.getCommandName());
        if (method == null) {
            log.error("command not found " + command.getCommandName());
            return new CommandRsp(CommandRsp.FAIL, "command not found");
        }

        try {

            return (CommandRsp) method.invoke(this, command.getParas());

        } catch (IllegalAccessException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return new CommandRsp(CommandRsp.FAIL, e.getCause());
        } catch (InvocationTargetException e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return new CommandRsp(CommandRsp.FAIL, e.getCause());
        } catch (Exception e) {
            log.error(ExceptionUtil.getBriefStackTrace(e));
            return new CommandRsp(CommandRsp.FAIL, e.getCause());
        }
    }

    /**
     * 获取设备实例
     *
     * @return 设备实例
     */
    public IoTDevice getIotDevice() {
        return iotDevice;
    }

    /**
     * 设置设备实例
     *
     * @param iotDevice 设备实例
     */
    public void setIotDevice(IoTDevice iotDevice) {
        this.iotDevice = iotDevice;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 开启自动周期上报属性
     *
     * @param reportInterval 上报周期，单位ms
     */
    public void enableAutoReport(int reportInterval) {
        if (timer != null) {
            log.error("timer is already enabled");
            return;
        }

        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    firePropertiesChanged();
                }
            }, reportInterval, reportInterval);
        }
    }

    /**
     * 关闭自动周期上报，您可以通过firePropertiesChanged触发上报
     */
    public void disableAutoReport() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}