package com.zyzylong.seeksome.admin.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.zyzylong.seeksome.admin.constant.AdminAuthConstant;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 按客户端 IP 统计登录失败次数，仅在内存窗口期内生效。
 */
@Component
public class IpLoginFailureCounter {

    private final Cache<String, AtomicInteger> failureCache =
            CacheUtil.newTimedCache(Duration.ofMinutes(AdminAuthConstant.IP_WINDOW_MINUTES).toMillis());

    public boolean isRateLimited(String clientIp) {
        AtomicInteger count = failureCache.get(clientIp);
        return count != null && count.get() >= AdminAuthConstant.IP_FAILURE_LIMIT;
    }

    public void recordFailure(String clientIp) {
        failureCache.get(clientIp, AtomicInteger::new).incrementAndGet();
    }
}
