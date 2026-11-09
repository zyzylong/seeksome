package com.zyzylong.seeksome.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.zyzylong.seeksome.auth.controller.app.request.AppLoginRequest;
import com.zyzylong.seeksome.auth.controller.app.request.AppRegisterRequest;
import com.zyzylong.seeksome.auth.controller.app.vo.AppLoginVO;
import com.zyzylong.seeksome.auth.enums.AppAuthErrorCodeEnum;
import com.zyzylong.seeksome.auth.exception.AppAuthException;
import com.zyzylong.seeksome.auth.service.AppAuthService;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.enums.UserStatusEnum;
import com.zyzylong.seeksome.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 用户端认证服务实现，负责注册、密码校验与会话签发。
 */
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private static final String TIMING_DUMMY_HASH = BCrypt.hashpw("seeksome-timing-dummy", BCrypt.gensalt());

    private final UserService userService;

    @Override
    public Long saveUser(AppRegisterRequest requestParam) {
        String username = requestParam.getUsername().strip();
        if (userService.getUserByUsername(username) != null) {
            throw new AppAuthException(AppAuthErrorCodeEnum.USERNAME_EXISTS);
        }
        UserDO user = UserDO.builder()
                .username(username)
                .password(BCrypt.hashpw(requestParam.getPassword(), BCrypt.gensalt()))
                .nickname(requestParam.getNickname().strip())
                .status(UserStatusEnum.NORMAL.getValue())
                .build();
        try {
            return userService.saveUser(user);
        } catch (DuplicateKeyException ex) {
            throw new AppAuthException(AppAuthErrorCodeEnum.USERNAME_EXISTS);
        }
    }

    @Override
    public AppLoginVO login(AppLoginRequest requestParam) {
        UserDO user = userService.getUserByUsername(requestParam.getUsername().strip());
        String hashedPassword = user == null ? TIMING_DUMMY_HASH : user.getPassword();
        boolean passwordMatched = passwordMatches(requestParam.getPassword(), hashedPassword);
        if (user == null || !passwordMatched) {
            throw new AppAuthException(AppAuthErrorCodeEnum.LOGIN_FAILED);
        }
        userService.checkUserAccessible(user.getId());

        StpUtil.login(user.getId());
        return AppLoginVO.builder()
                .token(StpUtil.getTokenInfo().getTokenValue())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    private boolean passwordMatches(String rawPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
