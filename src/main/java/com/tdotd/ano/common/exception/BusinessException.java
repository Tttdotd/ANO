package com.tdotd.ano.common.exception;

/**
 * 业务失败场景抛出，由 {@link com.tdotd.ano.common.advice.GlobalExceptionHandler} 转为 {@link com.tdotd.ano.common.result.Result}。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
