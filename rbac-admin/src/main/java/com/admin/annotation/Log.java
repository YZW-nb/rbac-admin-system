package com.admin.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用法：@Log(title = "用户管理", businessType = BusinessType.INSERT)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 操作模块 */
    String title() default "";

    /** 业务操作类型 */
    BusinessType businessType() default BusinessType.OTHER;
}
