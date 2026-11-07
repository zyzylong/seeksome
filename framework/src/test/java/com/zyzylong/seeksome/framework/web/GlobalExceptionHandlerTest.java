package com.zyzylong.seeksome.framework.web;

import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.exception.AbstractException;
import com.zyzylong.seeksome.framework.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void abstract_exception_maps_to_its_error_code_and_http_status() {
        ResponseEntity<Result<Void>> response =
                handler.handleAbstractException(new AbstractException(CommonErrorCodeEnum.UNAUTHORIZED));
        assertEquals(401, response.getStatusCode().value());
        assertEquals("A0401", response.getBody().getCode());
    }

    @Test
    void unexpected_exception_maps_to_500_and_system_error_code() {
        ResponseEntity<Result<Void>> response = handler.handleException(new IllegalStateException("boom"));
        assertEquals(500, response.getStatusCode().value());
        assertEquals("B0500", response.getBody().getCode());
    }
}
