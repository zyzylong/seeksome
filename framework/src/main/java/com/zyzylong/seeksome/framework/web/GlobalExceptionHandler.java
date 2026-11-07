package com.zyzylong.seeksome.framework.web;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.exception.AbstractException;
import com.zyzylong.seeksome.framework.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理，将异常统一转换为 Result 响应结构。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<Result<Void>> handleAbstractException(AbstractException ex) {
        BaseErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(Result.failure(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(CommonErrorCodeEnum.PARAM_INVALID.getMessage());
        return ResponseEntity.status(CommonErrorCodeEnum.PARAM_INVALID.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.PARAM_INVALID, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(CommonErrorCodeEnum.PARAM_INVALID.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.PARAM_INVALID));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(CommonErrorCodeEnum.NOT_FOUND.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.NOT_FOUND));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(CommonErrorCodeEnum.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(CommonErrorCodeEnum.UNSUPPORTED_MEDIA_TYPE.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(CommonErrorCodeEnum.SYSTEM_ERROR.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.SYSTEM_ERROR));
    }
}
