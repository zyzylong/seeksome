package com.zyzylong.seeksome.auth.service;

import com.zyzylong.seeksome.auth.controller.app.request.AppLoginRequest;
import com.zyzylong.seeksome.auth.controller.app.request.AppRegisterRequest;
import com.zyzylong.seeksome.auth.controller.app.vo.AppLoginVO;

/**
 * 用户端认证服务。
 */
public interface AppAuthService {

    Long saveUser(AppRegisterRequest requestParam);

    AppLoginVO login(AppLoginRequest requestParam);

    void logout();
}
