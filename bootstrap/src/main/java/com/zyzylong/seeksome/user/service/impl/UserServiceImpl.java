package com.zyzylong.seeksome.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.user.controller.admin.request.AdminUserPageQueryRequest;
import com.zyzylong.seeksome.user.controller.admin.vo.AdminUserVO;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import com.zyzylong.seeksome.user.enums.UserErrorCodeEnum;
import com.zyzylong.seeksome.user.enums.UserStatusEnum;
import com.zyzylong.seeksome.user.exception.UserException;
import com.zyzylong.seeksome.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public PageResult<AdminUserVO> listUsers(AdminUserPageQueryRequest requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        String keyword = StrUtil.trimToNull(requestParam.getKeyword());
        if (keyword != null) {
            queryWrapper.and(each -> each.like(UserDO::getUsername, keyword)
                    .or()
                    .like(UserDO::getNickname, keyword));
        }
        queryWrapper.orderByDesc(UserDO::getCreateTime).orderByDesc(UserDO::getId);

        Page<UserDO> page = userMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AdminUserVO> list = page.getRecords().stream().map(this::toAdminUserVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    @Override
    public void updateUserStatus(Long userId, UserStatusEnum userStatus) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(CommonErrorCodeEnum.NOT_FOUND);
        }
        if (userStatus.getValue().equals(user.getStatus())) {
            return;
        }
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .set(UserDO::getStatus, userStatus.getValue()));
    }

    @Override
    public void checkUserAccessible(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(CommonErrorCodeEnum.NOT_FOUND);
        }
        if (UserStatusEnum.FROZEN.getValue().equals(user.getStatus())) {
            throw new UserException(UserErrorCodeEnum.USER_FROZEN);
        }
    }

    @Override
    public UserDO getUserByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, username));
    }

    @Override
    public Long saveUser(UserDO requestParam) {
        userMapper.insert(requestParam);
        return requestParam.getId();
    }

    private AdminUserVO toAdminUserVO(UserDO user) {
        return AdminUserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .creditScore(user.getCreditScore())
                .walletBalance(user.getWalletBalance())
                .frozenBalance(user.getFrozenBalance())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }
}
