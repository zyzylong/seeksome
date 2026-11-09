package com.zyzylong.seeksome.auth.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 管理员账户持久层。
 */
public interface AdminUserMapper extends BaseMapper<AdminUserDO> {

    @Select("SELECT * FROM admin_user WHERE username = #{username} LIMIT 1")
    AdminUserDO getByUsername(@Param("username") String username);

    @Update("UPDATE admin_user SET " +
            "locked_until = CASE WHEN failed_count + 1 >= #{maxFailures} THEN #{lockedUntil} ELSE locked_until END, " +
            "failed_count = failed_count + 1, " +
            "update_time = #{now} WHERE id = #{adminId}")
    int incrementFailedCount(@Param("adminId") Long adminId, @Param("maxFailures") int maxFailures,
                             @Param("lockedUntil") LocalDateTime lockedUntil, @Param("now") LocalDateTime now);

    @Update("UPDATE admin_user SET failed_count = 0, locked_until = NULL, update_time = #{now} WHERE id = #{adminId}")
    int resetFailedCount(@Param("adminId") Long adminId, @Param("now") LocalDateTime now);
}
