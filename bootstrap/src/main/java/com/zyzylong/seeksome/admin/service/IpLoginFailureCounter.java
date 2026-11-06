package com.zyzylong.seeksome.admin.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.zyzylong.seeksome.admin.constant.AdminAuthConstant;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 按客户端 IP 统计登录失败次数，仅在内存窗口期内生效。
 */
@Component
public class IpLoginFailureCounter {

    private final TimedCache<String, AtomicInteger> failureCache;

    public IpLoginFailureCounter() {
        this(CacheUtil.newTimedCache(Duration.ofMinutes(AdminAuthConstant.IP_WINDOW_MINUTES).toMillis()),
                Duration.ofMinutes(AdminAuthConstant.IP_PRUNE_INTERVAL_MINUTES).toMillis());
    }

    IpLoginFailureCounter(TimedCache<String, AtomicInteger> failureCache, long pruneIntervalMillis) {
        this.failureCache = failureCache;
        failureCache.schedulePrune(pruneIntervalMillis);
    }

    public boolean isRateLimited(String clientIp) {
        AtomicInteger count = failureCache.get(clientIp, false);
        return count != null && count.get() >= AdminAuthConstant.IP_FAILURE_LIMIT;
    }

    public void recordFailure(String clientIp) {
        failureCache.get(clientIp, false, AtomicInteger::new).incrementAndGet();
    }
}
