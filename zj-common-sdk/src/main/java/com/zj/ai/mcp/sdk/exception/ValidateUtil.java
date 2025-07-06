package com.zj.ai.mcp.sdk.exception;


public class ValidateUtil {
    private ValidateUtil() {
    }

    /**
     * 断言对象不为空，为空则抛出异常
     *
     * @param object       对象
     * @param exception 状态码枚举类
     * @throws BusinessException 系统异常
     */
    public static void exceptionByNotNull(Object object, ResultCode exception) {
        if (object != null) {
            throw new BusinessException(exception);
        }
    }

    /**
     * 断言对象为空，否则抛出异常
     *
     * @param object       对象
     * @param httpCodeEnum 状态码枚举类
     * @throws BusinessException 系统异常
     */
    public static void exceptionByNull(Object object, ResultCode httpCodeEnum) {
        if (object == null) {
            throw new BusinessException(httpCodeEnum);
        }
    }

    /**
     * 请求非空对象，如果为空，抛出对应的 httpCodeEnum 对象。如果不为空，直接返回对应的对象
     * @param obj 需要判断的对象
     * @param <T> 对象的类型
     * @return {@code obj} if not {@code null}
     * @throws BusinessException if {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj, ResultCode httpCodeEnum) {
        if (obj == null)
            throw new BusinessException(httpCodeEnum);
        return obj;
    }


    /**
     * 请求非空对象，如果为空，抛出对应的 httpCodeEnum 对象。如果不为空，直接返回对应的对象
     * @param obj 需要判断的对象
     * @param <T> 对象的类型
     * @return {@code obj} if not {@code null}
     * @throws BusinessException if {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj, String errorMessage) {
        if (obj == null)
            throw new BusinessException(errorMessage);
        return obj;
    }

    public static void exceptionByTrue(Boolean flag, ResultCode resultCode) {
        if (Boolean.TRUE.equals(flag)) {
            throw new BusinessException(resultCode);
        }
    }

    public static void exceptionByTrue(Boolean flag, String errorMessage) {
        if (Boolean.TRUE.equals(flag)) {
            throw new BusinessException(errorMessage);
        }
    }

    public static void exceptionByFalse(Boolean flag, ResultCode resultCode) {
        if (Boolean.FALSE.equals(flag)) {
            throw new BusinessException(resultCode);
        }
    }

    public static void exceptionByFalse(Boolean flag, String errorMessage) {
        if (Boolean.FALSE.equals(flag)) {
            throw new BusinessException(errorMessage);
        }
    }
}
