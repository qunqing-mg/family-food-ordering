package com.familyfood.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBiz(BizException e) {
        log.warn("业务异常: {} {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        String msg = e.getMessage();
        if (msg != null && msg.contains("NullPointerException")) {
            return Result.fail(500, "系统内部错误，请检查输入是否完整");
        }
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
