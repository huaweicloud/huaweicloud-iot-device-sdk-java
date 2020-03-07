package com.huaweicloud.sdk.iot.device.service;

import java.lang.annotation.*;


/**
 * 设备命令
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DeviceCommand {

    /**
     * @return 命令名，不提供默认为方法名
     */
    String name() default "";
}