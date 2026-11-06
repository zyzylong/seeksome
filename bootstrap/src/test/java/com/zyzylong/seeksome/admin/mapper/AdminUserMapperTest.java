package com.zyzylong.seeksome.admin.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zyzylong.seeksome.admin.domain.dataobject.AdminUserDO;
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

    private AdminUserDO newAdmin(String username, int status) {
        AdminUserDO admin = new AdminUserDO();
        admin.setUsername(username);
        admin.setPassword("$2a$10$01234567890123456789012345678901234567890123456789012");
        admin.setRole("ADMIN");
        admin.setStatus(status);
        admin.setFailedCount(0);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        return admin;
    }
}
