package com.linguacraft.backend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器:把各类异常统一转换为前端契约的统一响应。
 *
 * <p>约定:
 * <ul>
 *   <li>业务错误:HTTP 200 + 业务 code(与前端 mock 契约一致,前端统一弹出 message);</li>
 *   <li>认证失败(code=401):HTTP 401,前端拦截后清 token 跳登录页;</li>
 *   <li>未捕获异常:记录完整日志,返回 code=500,不向前端泄漏堆栈细节。</li>
 * </ul></p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常:按 code 决定 HTTP 状态(仅 401 特殊处理为 HTTP 401)。
     *
     * @param e 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException e) {
        HttpStatus status = e.getCode() == ResultCode.UNAUTHORIZED.getCode()
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(R.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 请求体 JSON 解析失败:按参数错误处理。
     *
     * @param e 解析异常
     * @return 参数错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return R.fail(ResultCode.PARAM_ERROR.getCode(), "请求体格式错误");
    }

    /**
     * @Valid 参数校验失败:取第一条字段错误信息返回。
     *
     * @param e 校验异常
     * @return 参数错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ResultCode.PARAM_ERROR.getMessage();
        return R.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 静态资源/路径不存在:返回统一的 404 业务码。
     *
     * @param e 资源不存在异常
     * @return 404 业务响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResource(NoResourceFoundException e) {
        return R.fail(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底:未捕获异常统一转 500,记录完整日志便于排查。
     *
     * @param e 未捕获异常
     * @return 500 业务响应
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未捕获异常: {}", e.getMessage(), e);
        return R.fail(ResultCode.SERVER_ERROR);
    }
}