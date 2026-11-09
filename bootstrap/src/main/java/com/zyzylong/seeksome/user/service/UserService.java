package com.zyzylong.seeksome.user.service;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.user.controller.admin.request.AdminUserPageQueryRequest;
import com.zyzylong.seeksome.user.controller.admin.vo.AdminUserVO;
import com.zyzylong.seeksome.user.controller.app.request.AppCreditLogPageQueryRequest;
import com.zyzylong.seeksome.user.controller.app.request.AppUserProfileUpdateRequest;
import com.zyzylong.seeksome.user.controller.app.vo.AppCreditLogVO;
import com.zyzylong.seeksome.user.controller.app.vo.AppUserProfileVO;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.enums.UserStatusEnum;

/**
 * 用户服务。
 */
public interface UserService {

    PageResult<AdminUserVO> listUsers(AdminUserPageQueryRequest requestParam);

    AppUserProfileVO getUserProfile(Long userId);

    AppUserProfileVO updateUserProfile(Long userId, AppUserProfileUpdateRequest requestParam);

    PageResult<AppCreditLogVO> listCreditLogs(Long userId, AppCreditLogPageQueryRequest requestParam);

    void updateUserStatus(Long userId, UserStatusEnum userStatus);

    void checkUserAccessible(Long userId);

    UserDO getUserByUsername(String username);

    Long saveUser(UserDO requestParam);
}
