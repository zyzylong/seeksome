package com.zyzylong.seeksome.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.zyzylong.seeksome.admin.auth.StpAdminUtil;
import com.zyzylong.seeksome.admin.constant.AdminAuthConstant;
import com.zyzylong.seeksome.admin.domain.dataobject.AdminUserDO;
import com.zyzylong.seeksome.admin.domain.dto.AdminInfoRespDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginReqDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginRespDTO;
import com.zyzylong.seeksome.admin.domain.enums.AdminAuthErrorCodeEnum;
import com.zyzylong.seeksome.admin.domain.enums.AdminStatusEnum;
import com.zyzylong.seeksome.admin.exception.AdminAuthException;
import com.zyzylong.seeksome.admin.mapper.AdminUserMapper;
import com.zyzylong.seeksome.admin.service.AdminAuthService;
import com.zyzylong.seeksome.admin.service.IpLoginFailureCounter;
import com.zyzylong.seeksome.framework.common.error.CommonErrorCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员认证服务实现，负责密码校验、锁定、限流与审计。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final String UNKNOWN_USER_AGENT = "unknown";

    private static final String TIMING_DUMMY_HASH = BCrypt.hashpw("seeksome-timing-dummy", BCrypt.gensalt());

    private final AdminUserMapper adminUserMapper;

    private final IpLoginFailureCounter ipLoginFailureCounter;

    @Override
    public AdminLoginRespDTO login(AdminLoginReqDTO requestParam, String clientIp, String userAgent) {
        String username = requestParam.getUsername().strip();
        String resolvedUserAgent = StrUtil.isBlank(userAgent) ? UNKNOWN_USER_AGENT : userAgent;
        LocalDateTime now = LocalDateTime.now();

        if (ipLoginFailureCounter.isRateLimited(clientIp)) {
            writeAudit("ip-rate-limited", null, username, clientIp, resolvedUserAgent,
                    "rejected", "ip-failure-threshold");
            throw new AdminAuthException(AdminAuthErrorCodeEnum.IP_RATE_LIMITED);
        }

        AdminUserDO admin = adminUserMapper.getByUsername(username);
        if (admin != null && admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(now)) {
            writeAudit("account-locked", admin.getId(), username, clientIp, resolvedUserAgent,
                    "rejected", "locked");
            throw new AdminAuthException(AdminAuthErrorCodeEnum.ACCOUNT_LOCKED);
        }
        if (admin != null && admin.getLockedUntil() != null) {
            adminUserMapper.resetFailedCount(admin.getId(), now);
            admin.setFailedCount(0);
            admin.setLockedUntil(null);
        }

        String hashedPassword = admin == null ? TIMING_DUMMY_HASH : admin.getPassword();
        boolean passwordMatched = passwordMatches(requestParam.getPassword(), hashedPassword);
        if (admin == null || !passwordMatched) {
            ipLoginFailureCounter.recordFailure(clientIp);
            if (admin != null) {
                adminUserMapper.incrementFailedCount(admin.getId(), AdminAuthConstant.MAX_LOGIN_FAILURES,
                        now.plusMinutes(AdminAuthConstant.LOCK_MINUTES), now);
                if (admin.getFailedCount() + 1 >= AdminAuthConstant.MAX_LOGIN_FAILURES) {
                    writeAudit("account-locked", admin.getId(), username, clientIp, resolvedUserAgent,
                            "rejected", "threshold-reached");
                }
            }
            writeAudit("login-failure", admin == null ? null : admin.getId(), username, clientIp,
                    resolvedUserAgent, "failed", "invalid-credentials");
            throw new AdminAuthException(AdminAuthErrorCodeEnum.LOGIN_FAILED);
        }

        if (!AdminStatusEnum.ENABLED.getValue().equals(admin.getStatus())) {
            writeAudit("account-disabled", admin.getId(), username, clientIp, resolvedUserAgent,
                    "rejected", "disabled");
            throw new AdminAuthException(AdminAuthErrorCodeEnum.ACCOUNT_DISABLED);
        }

        adminUserMapper.resetFailedCount(admin.getId(), now);
        StpAdminUtil.login(admin.getId());
        String token = StpAdminUtil.getTokenInfo().getTokenValue();
        writeAudit("login-success", admin.getId(), username, clientIp, resolvedUserAgent,
                "success", "none");

        AdminLoginRespDTO result = new AdminLoginRespDTO();
        result.setToken(token);
        result.setAdminId(admin.getId());
        result.setUsername(admin.getUsername());
        result.setRole(admin.getRole());
        return result;
    }

    @Override
    public AdminInfoRespDTO getCurrentAdminInfo(Long adminId) {
        AdminUserDO admin = adminUserMapper.selectById(adminId);
        if (admin == null || !AdminStatusEnum.ENABLED.getValue().equals(admin.getStatus())) {
            StpAdminUtil.logout();
            throw new AdminAuthException(CommonErrorCodeEnum.UNAUTHORIZED);
        }
        AdminInfoRespDTO result = new AdminInfoRespDTO();
        result.setAdminId(admin.getId());
        result.setUsername(admin.getUsername());
        result.setRole(admin.getRole());
        return result;
    }

    @Override
    public void logout(Long adminId, String clientIp, String userAgent) {
        StpAdminUtil.logout();
        String resolvedUserAgent = StrUtil.isBlank(userAgent) ? UNKNOWN_USER_AGENT : userAgent;
        writeAudit("logout", adminId, null, clientIp, resolvedUserAgent, "success", "none");
    }

    private boolean passwordMatches(String rawPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void writeAudit(String event, Long adminId, String username, String clientIp, String userAgent,
                            String result, String reason) {
        log.info("admin-auth audit: event={}, adminId={}, username={}, ip={}, ua={}, result={}, reason={}",
                event, adminId, username, clientIp, userAgent, result, reason);
    }
}
