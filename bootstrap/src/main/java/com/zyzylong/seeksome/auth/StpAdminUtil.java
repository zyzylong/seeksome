package com.zyzylong.seeksome.auth;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.bean.BeanUtil;
import com.zyzylong.seeksome.auth.constant.AdminAuthConstant;

/**
 * 管理员会话操作入口，使用独立 StpLogic 与普通用户登录态隔离。
 */
public final class StpAdminUtil {

    public static final StpLogic STP_LOGIC = new StpLogic(AdminAuthConstant.ADMIN_LOGIN_TYPE);

    private static volatile boolean configured = false;

    private StpAdminUtil() {
    }

    public static void login(Long adminId) {
        ensureConfigured();
        STP_LOGIC.login(adminId);
    }

    public static void logout() {
        ensureConfigured();
        STP_LOGIC.logout();
    }

    public static Long getLoginIdAsLong() {
        ensureConfigured();
        return STP_LOGIC.getLoginIdAsLong();
    }

    public static void checkLogin() {
        ensureConfigured();
        STP_LOGIC.checkLogin();
    }

    public static SaTokenInfo getTokenInfo() {
        ensureConfigured();
        return STP_LOGIC.getTokenInfo();
    }

    private static void ensureConfigured() {
        if (configured) {
            return;
        }
        synchronized (StpAdminUtil.class) {
            if (configured) {
                return;
            }
            SaTokenConfig adminConfig = BeanUtil.copyProperties(SaManager.getConfig(), SaTokenConfig.class);
            adminConfig.setTokenName(AdminAuthConstant.ADMIN_TOKEN_NAME);
            STP_LOGIC.setConfig(adminConfig);
            configured = true;
        }
    }
}
