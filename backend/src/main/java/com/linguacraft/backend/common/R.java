package com.linguacraft.backend.common;

import lombok.Getter;

/**
 * 统一响应包装:所有接口返回 {code, message, data} 结构(前端契约约定)。
 *
 * @param <T> 业务数据类型
 */
@Getter
public class R<T> {

    /** 业务状态码:0 表示成功,非 0 视为错误(码值定义见 ResultCode) */
    private final int code;

    /** 提示信息:失败时供前端直接弹出(前端对 code!==0 统一弹错误提示) */
    private final String message;

    /** 业务数据:失败时为 null */
    private final T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应。
     *
     * @param data 业务数据
     * @return code=0 的统一响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    /**
     * 成功响应(无数据)。
     *
     * @return code=0、data 为 null 的统一响应
     */
    public static R<Void> ok() {
        return ok(null);
    }

    /**
     * 失败响应。
     *
     * @param code    业务错误码
     * @param message 可读错误信息
     * @return 失败响应,data 固定为 null
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败响应(使用预定义错误码)。
     *
     * @param resultCode 预定义错误码枚举
     * @return 失败响应,data 固定为 null
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }
}