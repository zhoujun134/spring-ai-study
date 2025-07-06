package com.zj.ai.mcp.sdk.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * @author junzhou
 * @date 2022/9/18 10:43
 * @since 1.8
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ResultCode {

    /**
     * 通用类 异常
     */
    SUCCESS("0", "ok"),

    FAIL("-1", "请求错误，请联系管理员！"),

    ;

    /**
     * 异常 code
     */
    private String code;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 根据 code 获取 resultCode 异常信息
     *
     * @param code 报错 code
     * @return 对应 code 的异常结果
     */
    public static ResultCode getByCode(String code) {
        return Stream.of(ResultCode.values())
                .filter(resultCode -> StringUtils.equals(resultCode.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取带参数的 resultCode 信息
     *
     * @param resultCode 原始的 resultCode
     * @param args       参数信息
     * @return 更新参数后的 resultCode
     */
    public static ResultCode getResultCode(ResultCode resultCode, Object... args) {
        String message = resultCode.getMessage();
        resultCode.message = String.format(message, args);
        return resultCode;
    }

    /**
     * 获取带参数的 resultCode 信息
     *
     * @param resultCode 原始的 resultCode
     * @param args       参数信息
     * @return 更新参数后的 resultCode
     */
    public static ResultCode getResultCode(ResultCode resultCode, Object args) {
        String message = resultCode.getMessage();
        resultCode.message = String.format(message, args);
        return resultCode;
    }
}
