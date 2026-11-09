package com.zyzylong.seeksome.user.service;

import cn.hutool.crypto.digest.BCrypt;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import com.zyzylong.seeksome.user.enums.UserErrorCodeEnum;
import com.zyzylong.seeksome.user.exception.UserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    void checkUserAccessible_allows_normal_user() {
        Long userId = saveUser("user_access_normal", "normal").getId();

        assertDoesNotThrow(() -> userService.checkUserAccessible(userId));
    }

    @Test
    void checkUserAccessible_rejects_frozen_user_for_login_and_trade() {
        Long userId = saveUser("user_access_frozen", "frozen").getId();

        UserException exception = assertThrows(UserException.class,
                () -> userService.checkUserAccessible(userId));

        assertEquals(UserErrorCodeEnum.USER_FROZEN, exception.getErrorCode());
    }

    @Test
    void checkUserAccessible_rejects_unknown_user() {
        UserException exception = assertThrows(UserException.class,
                () -> userService.checkUserAccessible(999999L));

        assertEquals(CommonErrorCodeEnum.NOT_FOUND, exception.getErrorCode());
    }

    private UserDO saveUser(String username, String userStatus) {
        UserDO user = UserDO.builder()
                .username(username)
                .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                .nickname(username)
                .creditScore(100)
                .walletBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .status(userStatus)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }
}
