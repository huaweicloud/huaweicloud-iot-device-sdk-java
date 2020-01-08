package com.huaweicloud.sdk.iot.device.service;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Property {

    /**
     * 属性是否可写。注：所有属性默认都可读
     *
     * @return true表示可写
     */
    boolean writeable() default true;

    /**
     * @return 属性名，不提供默认为字段名
     */
    String name() default "";

}