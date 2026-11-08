package com.zyzylong.seeksome.user.service;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.user.controller.admin.request.AdminUserPageQueryRequest;
import com.zyzylong.seeksome.user.controller.admin.vo.AdminUserVO;

/**
 * 用户服务。
 */
public interface UserService {

    PageResult<AdminUserVO> listUsers(AdminUserPageQueryRequest requestParam);
}
