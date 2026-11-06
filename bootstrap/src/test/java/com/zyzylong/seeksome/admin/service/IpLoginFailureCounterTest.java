package com.zyzylong.seeksome.admin.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void reads_do_not_extend_the_failure_window() throws InterruptedException {
        IpLoginFailureCounter shortWindowCounter =
                new IpLoginFailureCounter(CacheUtil.newTimedCache(300), 60_000);
        for (int i = 0; i < 19; i++) {
            shortWindowCounter.recordFailure("10.0.0.3");
        }
        long deadline = System.currentTimeMillis() + 400;
        while (System.currentTimeMillis() < deadline) {
            shortWindowCounter.isRateLimited("10.0.0.3");
        }
        shortWindowCounter.recordFailure("10.0.0.3");
        assertFalse(shortWindowCounter.isRateLimited("10.0.0.3"));
    }

    @Test
    void expired_entries_are_pruned_periodically() throws InterruptedException {
        TimedCache<String, AtomicInteger> failureCache = CacheUtil.newTimedCache(100);
        IpLoginFailureCounter prunedCounter = new IpLoginFailureCounter(failureCache, 50);
        for (int i = 0; i < 50; i++) {
            prunedCounter.recordFailure("10.11.0." + i);
        }
        assertEquals(50, failureCache.size());
        Thread.sleep(400);
        assertEquals(0, failureCache.size());
    }
}
