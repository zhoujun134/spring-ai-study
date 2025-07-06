package com.zj.ai.mcp.sdk.exception;

/**
 * @author junzhou
 * @date 2022/9/18 18:34
 * @since 1.8
 * 匿名用户异常
 */
public class AnonymousException extends RuntimeException{

    public AnonymousException(String message) {
        super(message);
    }

    public AnonymousException() {
    }
}
