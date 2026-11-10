package com.zyzylong.seeksome.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.user.controller.admin.request.AdminUserPageQueryRequest;
import com.zyzylong.seeksome.user.controller.admin.vo.AdminUserVO;
import com.zyzylong.seeksome.user.controller.app.request.AppCreditLogPageQueryRequest;
import com.zyzylong.seeksome.user.controller.app.request.AppUserProfileUpdateRequest;
import com.zyzylong.seeksome.user.controller.app.vo.AppCreditLogVO;
import com.zyzylong.seeksome.user.controller.app.vo.AppUserProfileVO;
import com.zyzylong.seeksome.user.dao.entity.CreditLogDO;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.CreditLogMapper;
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

    private final CreditLogMapper creditLogMapper;

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
        if (!userStatus.getValue().equals(user.getStatus())) {
            userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                    .eq(UserDO::getId, userId)
                    .set(UserDO::getStatus, userStatus.getValue()));
        }
        if (UserStatusEnum.FROZEN.equals(userStatus)) {
            StpUtil.logout(userId);
        }
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

    @Override
    public AppUserProfileVO getUserProfile(Long userId) {
        return toAppUserProfileVO(getExistingUser(userId));
    }

    @Override
    public AppUserProfileVO updateUserProfile(Long userId, AppUserProfileUpdateRequest requestParam) {
        getExistingUser(userId);
        LambdaUpdateWrapper<UserDO> updateWrapper = new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .set(UserDO::getNickname, requestParam.getNickname().strip());
        if (requestParam.getAvatar() != null) {
            updateWrapper.set(UserDO::getAvatar, StrUtil.trimToNull(requestParam.getAvatar()));
        }
        userMapper.update(null, updateWrapper);
        return toAppUserProfileVO(getExistingUser(userId));
    }

    @Override
    public PageResult<AppCreditLogVO> listCreditLogs(Long userId,
                                                     AppCreditLogPageQueryRequest requestParam) {
        LambdaQueryWrapper<CreditLogDO> queryWrapper = new LambdaQueryWrapper<CreditLogDO>()
                .eq(CreditLogDO::getUserId, userId)
                .orderByDesc(CreditLogDO::getCreateTime)
                .orderByDesc(CreditLogDO::getId);
        Page<CreditLogDO> page = creditLogMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AppCreditLogVO> list = page.getRecords().stream().map(this::toAppCreditLogVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    private UserDO getExistingUser(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new UserException(CommonErrorCodeEnum.NOT_FOUND);
        }
        return user;
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

    private AppUserProfileVO toAppUserProfileVO(UserDO user) {
        return AppUserProfileVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .creditScore(user.getCreditScore())
                .createTime(user.getCreateTime())
                .build();
    }

    private AppCreditLogVO toAppCreditLogVO(CreditLogDO creditLog) {
        return AppCreditLogVO.builder()
                .creditLogId(creditLog.getId())
                .changeValue(creditLog.getChangeValue())
                .reason(creditLog.getReason())
                .relatedType(creditLog.getRelatedType())
                .relatedId(creditLog.getRelatedId())
                .createTime(creditLog.getCreateTime())
                .build();
    }
}
