package com.admin.common.exception;

import com.admin.common.result.Result;
import com.admin.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 未认证异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUnauthorizedException(UnauthorizedException e) {
        return Result.error(ResultCode.UNAUTHORIZED);
    }

    /**
     * 参数校验异常 - @Valid @RequestBody
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 参数校验异常 - @Valid 表单
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 参数校验异常 - @Validated 单参数
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("约束违反异常: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持: {}", e.getMethod());
        return Result.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "请求方法不支持: " + e.getMethod());
    }

    /**
     * 请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("请求参数缺失: {}", e.getParameterName());
        return Result.error(HttpStatus.BAD_REQUEST.value(), "缺少必要参数: " + e.getParameterName());
    }

    /**
     * 请求内容类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.error("请求内容类型不支持: {}", e.getContentType());
        return Result.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "不支持的内容类型: " + e.getContentType());
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ResultCode.ERROR);
    }
}
