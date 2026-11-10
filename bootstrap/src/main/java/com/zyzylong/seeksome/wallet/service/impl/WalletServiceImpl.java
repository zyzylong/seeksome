package com.zyzylong.seeksome.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import com.zyzylong.seeksome.wallet.controller.app.request.AppWalletLogPageQueryRequest;
import com.zyzylong.seeksome.wallet.controller.app.request.AppWalletRechargeRequest;
import com.zyzylong.seeksome.wallet.controller.app.vo.AppWalletLogVO;
import com.zyzylong.seeksome.wallet.controller.app.vo.AppWalletVO;
import com.zyzylong.seeksome.wallet.dao.entity.WalletLogDO;
import com.zyzylong.seeksome.wallet.dao.mapper.WalletLogMapper;
import com.zyzylong.seeksome.wallet.enums.WalletLogTypeEnum;
import com.zyzylong.seeksome.wallet.exception.WalletException;
import com.zyzylong.seeksome.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 钱包服务实现。
 */
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final UserMapper userMapper;

    private final WalletLogMapper walletLogMapper;

    @Override
    public AppWalletVO getWallet(Long userId) {
        return toAppWalletVO(getExistingUser(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppWalletVO saveRecharge(Long userId, AppWalletRechargeRequest requestParam) {
        int result = userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .setSql("wallet_balance = wallet_balance + {0}", requestParam.getAmount()));
        if (result == 0) {
            throw new WalletException(CommonErrorCodeEnum.NOT_FOUND);
        }
        walletLogMapper.insert(WalletLogDO.builder()
                .userId(userId)
                .amount(requestParam.getAmount())
                .type(WalletLogTypeEnum.RECHARGE.getValue())
                .build());
        return toAppWalletVO(getExistingUser(userId));
    }

    @Override
    public PageResult<AppWalletLogVO> listWalletLogs(Long userId,
                                                     AppWalletLogPageQueryRequest requestParam) {
        LambdaQueryWrapper<WalletLogDO> queryWrapper = new LambdaQueryWrapper<WalletLogDO>()
                .eq(WalletLogDO::getUserId, userId)
                .orderByDesc(WalletLogDO::getCreateTime)
                .orderByDesc(WalletLogDO::getId);
        Page<WalletLogDO> page = walletLogMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AppWalletLogVO> list = page.getRecords().stream().map(this::toAppWalletLogVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    private UserDO getExistingUser(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new WalletException(CommonErrorCodeEnum.NOT_FOUND);
        }
        return user;
    }

    private AppWalletVO toAppWalletVO(UserDO user) {
        return AppWalletVO.builder()
                .walletBalance(user.getWalletBalance())
                .frozenBalance(user.getFrozenBalance())
                .build();
    }

    private AppWalletLogVO toAppWalletLogVO(WalletLogDO walletLog) {
        return AppWalletLogVO.builder()
                .walletLogId(walletLog.getId())
                .amount(walletLog.getAmount())
                .type(walletLog.getType())
                .relatedType(walletLog.getRelatedType())
                .relatedId(walletLog.getRelatedId())
                .createTime(walletLog.getCreateTime())
                .build();
    }
}
