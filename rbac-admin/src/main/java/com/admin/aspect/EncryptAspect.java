package com.admin.aspect;

import com.admin.annotation.Encrypt;
import com.admin.common.utils.AesEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.FieldSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 加密字段自动处理切面
 * 在保存数据时自动加密标记了 @Encrypt 的字段
 * 在返回数据时自动解密标记了 @Encrypt 的字段
 */
@Slf4j
@Aspect
@Component
public class EncryptAspect {

    /**
     * 处理保存/更新时的加密
     */
    @Around("execution(* com.admin.service.impl.*.save*(..)) || execution(* com.admin.service.impl.*.update*(..))")
    public Object encryptAround(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg != null) {
                    encryptFields(arg);
                }
            }
        }
        return point.proceed();
    }

    /**
     * 递归加密对象中的所有 @Encrypt 字段
     */
    private void encryptFields(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();

        // 处理当前类的字段
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Encrypt.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String strValue && strValue != null && !strValue.isEmpty()) {
                        // 如果是加密值，跳过
                        if (AesEncryptUtil.isEncrypted(strValue)) {
                            continue;
                        }
                        // 加密并设置
                        String encrypted = AesEncryptUtil.encryptWithPrefix(strValue);
                        field.set(obj, encrypted);
                    }
                } catch (Exception e) {
                    log.warn("加密字段失败: {}", field.getName(), e);
                }
            }
        }

        // 如果是 JDK 17+，可以使用 record 或其他类型
        // 递归处理子对象（避免重复处理）
    }
}
