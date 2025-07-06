package com.zj.ai.mcp.sdk.exception;

/**
 * @author junzhou
 * @date 2022/9/18 10:20
 * @since 1.8
 */
public class BusinessException extends RuntimeException {
    private String code = "-1";
    private final Object result;

    public BusinessException(String code, Object result) {
        this.code = code;
        this.result = result;
    }

    public BusinessException(String message, String code, Object result) {
        super(message);
        this.code = code;
        this.result = result;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.result = resultCode;
    }

    public BusinessException(String message) {
        super(message);
        this.result = message;
    }
}
