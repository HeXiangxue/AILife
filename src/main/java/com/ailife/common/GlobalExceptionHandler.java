package com.ailife.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BizException.class)
  public Result<Void> handleBizException(BizException e) {
    return Result.error(e.getCode(), e.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
    log.debug("静态资源不存在：{}", e.getResourcePath());
    return Result.error(404, "资源不存在");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleValidException(MethodArgumentNotValidException e) {
    FieldError fieldError = e.getBindingResult().getFieldError();
    String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
    return Result.error(400, msg);
  }

  @ExceptionHandler(Exception.class)
  public Result<Void> handleException(Exception e) {
    log.error("系统异常", e);
    return Result.error(500, "系统繁忙，请稍后再试");
  }
}
