package com.zyzylong.seeksome.admin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpLoginFailureCounterTest {

    private final IpLoginFailureCounter counter = new IpLoginFailureCounter();

    @Test
    void twenty_failures_from_same_ip_trigger_rate_limit_but_other_ip_is_clean() {
        for (int i = 0; i < 19; i++) {
            counter.recordFailure("10.0.0.1");
        }
        assertFalse(counter.isRateLimited("10.0.0.1"));
        counter.recordFailure("10.0.0.1");
        assertTrue(counter.isRateLimited("10.0.0.1"));
        assertFalse(counter.isRateLimited("10.0.0.2"));
    }
}
