package com.zyzylong.seeksome.admin.support;

import cn.dev33.satoken.stp.StpUtil;
import com.zyzylong.seeksome.framework.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试用普通用户会话探针，验证与管理员会话隔离。
 */
@RestController
@RequestMapping("/test/user")
public class UserSessionProbeController {

    @GetMapping("/login")
    public Result<Long> login(@RequestParam Long userId) {
        StpUtil.login(userId);
        return Result.success(userId);
    }

    @GetMapping("/me")
    public Result<Long> getCurrentUserId() {
        StpUtil.checkLogin();
        return Result.success(StpUtil.getLoginIdAsLong());
    }
}
