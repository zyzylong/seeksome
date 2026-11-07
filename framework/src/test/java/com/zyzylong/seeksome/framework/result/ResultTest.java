package com.zyzylong.seeksome.framework.result;

import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void success_returns_code_message_data_and_request_id_from_mdc() {
        MDC.put("requestId", "req-123");
        try {
            Result<String> result = Result.success("payload");
            assertEquals("00000", result.getCode());
            assertEquals("成功", result.getMessage());
            assertEquals("payload", result.getData());
            assertEquals("req-123", result.getRequestId());
        } finally {
            MDC.remove("requestId");
        }
    }

    @Test
    void failure_uses_error_code_values() {
        Result<Void> result = Result.failure(CommonErrorCodeEnum.UNAUTHORIZED);
        assertEquals("A0401", result.getCode());
        assertEquals("未登录或登录状态已失效", result.getMessage());
        assertNull(result.getData());
    }
}
