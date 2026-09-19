package com.linguacraft.backend.common;

import lombok.Getter;

/**
 * 业务异常:业务规则不满足时抛出,由全局异常处理器统一转换为统一响应。
 *
 * <p>约定:业务错误统一使用 HTTP 200 + 业务 code(与前端 mock 契约一致);
 * 仅 code=401 时使用 HTTP 401,便于前端 401 拦截(清 token 跳登录页)。</p>
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务状态码(见 ResultCode) */
    private final int code;

    /**
     * 构造业务异常。
     *
     * @param code    业务状态码
     * @param message 可读错误信息(返回给前端展示)
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常(使用预定义错误码)。
     *
     * @param resultCode 预定义错误码枚举
     */
    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }
}