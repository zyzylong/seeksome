package com.zyzylong.seeksome.framework.web;

import cn.dev33.satoken.exception.NotLoginException;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 未登录异常处理，统一返回 401。
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLoginException(NotLoginException ex) {
        return ResponseEntity.status(CommonErrorCodeEnum.UNAUTHORIZED.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.UNAUTHORIZED));
    }
}
