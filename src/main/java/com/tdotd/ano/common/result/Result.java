package com.tdotd.ano.common.result;

/**
 * 统一 API 响应，与 docs/api/ANO_api.md 约定一致。
 */
public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(0, message, data);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
