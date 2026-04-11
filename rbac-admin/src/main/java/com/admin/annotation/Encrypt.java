package com.admin.annotation;

import java.lang.annotation.*;

/**
 * 加密注解
 * 标记字段在返回时自动解密，在保存时自动加密
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypt {
}
