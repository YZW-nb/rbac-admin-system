package com.admin.common.exception;

import com.admin.common.result.ResultCode;

/**
 * 未认证异常
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED.getMessage());
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
