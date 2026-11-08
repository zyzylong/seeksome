package com.zyzylong.seeksome.auth.constant;

/**
 * 管理员认证相关常量。
 */
public final class AdminAuthConstant {

    public static final int MAX_LOGIN_FAILURES = 5;

    public static final int LOCK_MINUTES = 15;

    public static final int IP_FAILURE_LIMIT = 20;

    public static final int IP_WINDOW_MINUTES = 10;

    public static final int IP_PRUNE_INTERVAL_MINUTES = 1;

    private AdminAuthConstant() {
    }
}
