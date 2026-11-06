package com.zyzylong.seeksome.admin.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.zyzylong.seeksome.framework.common.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.common.result.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 未登录异常处理，统一返回 401。
 */
@RestControllerAdvice
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLoginException(NotLoginException ex) {
        return ResponseEntity.status(CommonErrorCodeEnum.UNAUTHORIZED.getHttpStatus())
                .body(Result.failure(CommonErrorCodeEnum.UNAUTHORIZED));
    }
}
