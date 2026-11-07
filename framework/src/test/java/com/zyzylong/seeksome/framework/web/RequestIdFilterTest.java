package com.zyzylong.seeksome.framework.web;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestIdFilterTest {

    @Test
    void filter_generates_request_id_sets_mdc_and_response_header() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        new RequestIdFilter().doFilter(request, response, (req, res) -> assertEquals(36, MDC.get("requestId").length()));
        assertNotNull(response.getHeader("X-Request-Id"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void filter_keeps_client_supplied_request_id() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "client-req-1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new RequestIdFilter().doFilter(request, response, (req, res) -> { });
        assertEquals("client-req-1", response.getHeader("X-Request-Id"));
    }
}
