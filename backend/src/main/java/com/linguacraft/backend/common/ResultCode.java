package com.linguacraft.backend.common;

import lombok.Getter;

/**
 * 业务状态码枚举。
 *
 * <p>码值沿用前端 mock 契约(00x / 10xx),保证前端提示逻辑与文案兼容:
 * code=401 时前端会清除 token 并跳转登录页;其余非 0 码前端统一弹出 message。</p>
 */
@Getter
public enum ResultCode {

    /** 成功 */
    OK(0, "ok"),

    /** 参数校验失败 */
    PARAM_ERROR(400, "参数校验失败"),

    /** 未登录或登录已过期(前端收到后清 token 跳登录页) */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 服务器内部错误 */
    SERVER_ERROR(500, "服务器内部错误,请稍后重试"),

    /** 登录失败:用户名或密码错误 */
    LOGIN_FAILED(1001, "用户名或密码错误"),

    /** 注册失败:用户名已存在 */
    USERNAME_EXISTS(1002, "用户名已存在"),

    /** 注册失败:用户名不能为空 */
    USERNAME_REQUIRED(1003, "用户名不能为空"),

    /** 题型参数缺失或不合法 */
    TYPE_INVALID(1004, "题型参数缺失或不合法"),

    /** 来源参数不合法(列表接口仅接受 real/teacher) */
    SOURCE_INVALID(1005, "source 仅接受 real 或 teacher");

    /** 业务状态码 */
    private final int code;

    /** 默认提示信息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}