package com.zyzylong.seeksome.auth.dao.mapper;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserMapperTest {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Test
    void insert_and_select_admin_user_by_username() {
        AdminUserDO admin = newAdmin("admin_mapper", 1);
        assertEquals(1, adminUserMapper.insert(admin));
        assertNotNull(admin.getId());
        AdminUserDO result = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUserDO>().eq(AdminUserDO::getUsername, "admin_mapper"));
        assertEquals("admin_mapper", result.getUsername());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    void duplicate_username_is_rejected() {
        adminUserMapper.insert(newAdmin("admin_dup", 1));
        assertThrows(DuplicateKeyException.class, () -> adminUserMapper.insert(newAdmin("admin_dup", 1)));
    }

    @Test
    void increment_failed_count_sql_reads_failed_count_before_increment() throws NoSuchMethodException {
        Update update = AdminUserMapper.class
                .getMethod("incrementFailedCount", Long.class, int.class, LocalDateTime.class, LocalDateTime.class)
                .getAnnotation(Update.class);
        String sql = String.join(" ", update.value());
        int lockAssignmentIndex = sql.indexOf("locked_until = CASE");
        int incrementAssignmentIndex = sql.indexOf("failed_count = failed_count + 1");
        assertTrue(lockAssignmentIndex >= 0, "SQL must assign locked_until via CASE");
        assertTrue(incrementAssignmentIndex >= 0, "SQL must increment failed_count");
        assertTrue(lockAssignmentIndex < incrementAssignmentIndex,
                "MySQL evaluates SET assignments left to right; locked_until must be evaluated before failed_count is incremented");
    }

    private AdminUserDO newAdmin(String username, int enabled) {
        return AdminUserDO.builder()
                .username(username)
                .password("$2a$10$01234567890123456789012345678901234567890123456789012")
                .role("ADMIN")
                .enabled(enabled)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testAdminUSerHash() {
        System.out.println(BCrypt.hashpw("123456", BCrypt.gensalt()));
        System.out.println(BCrypt.checkpw("123456", BCrypt.hashpw("123456", BCrypt.gensalt())));
    }
}
