package com.zyzylong.seeksome.user.controller.app;

import cn.hutool.crypto.digest.BCrypt;
import com.zyzylong.seeksome.user.dao.entity.CreditLogDO;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.CreditLogMapper;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppCreditLogApiTest {

    private static final String PASSWORD_HASH = BCrypt.hashpw("123456", BCrypt.gensalt());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditLogMapper creditLogMapper;

    @Test
    void list_requires_login() throws Exception {
        mockMvc.perform(get("/app/users/credit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_current_user_logs_newest_first() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        UserDO user = saveUser("credit_user");
        UserDO other = saveUser("credit_other");
        saveCreditLog(other.getId(), 9001L, -50, "其他用户违约", now.minusMinutes(10));
        saveCreditLog(user.getId(), 9002L, -20, "超时未支付，违约扣分", now.minusHours(2));
        saveCreditLog(user.getId(), 9003L, -10, "第二次违约", now.minusHours(1));
        String token = login(user.getId());

        mockMvc.perform(get("/app/users/credit-logs").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].changeValue").value(-10))
                .andExpect(jsonPath("$.data.list[0].reason").value("第二次违约"))
                .andExpect(jsonPath("$.data.list[0].relatedType").value("order"))
                .andExpect(jsonPath("$.data.list[0].relatedId").value(9003))
                .andExpect(jsonPath("$.data.list[0].createTime").exists())
                .andExpect(jsonPath("$.data.list[1].changeValue").value(-20))
                .andExpect(jsonPath("$.data.list[1].relatedId").value(9002));
    }

    @Test
    void list_uses_default_pagination() throws Exception {
        UserDO user = saveUser("credit_page_user");
        LocalDateTime now = LocalDateTime.now();
        for (int each = 0; each < 12; each++) {
            saveCreditLog(user.getId(), 9100L + each, -20, "违约 " + each, now.minusMinutes(20 - each));
        }
        String token = login(user.getId());

        mockMvc.perform(get("/app/users/credit-logs").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(10))
                .andExpect(jsonPath("$.data.list[0].relatedId").value(9111));
    }

    @Test
    void list_paginates_with_page_num_and_page_size() throws Exception {
        UserDO user = saveUser("credit_page2_user");
        LocalDateTime now = LocalDateTime.now();
        saveCreditLog(user.getId(), 9201L, -20, "较早", now.minusHours(2));
        saveCreditLog(user.getId(), 9202L, -20, "居中", now.minusHours(1));
        saveCreditLog(user.getId(), 9203L, -20, "较新", now.minusMinutes(1));
        String token = login(user.getId());

        mockMvc.perform(get("/app/users/credit-logs")
                        .header("satoken", token)
                        .param("pageNum", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].relatedId").value(9201));
    }

    @Test
    void list_returns_empty_page_when_user_has_no_logs() throws Exception {
        UserDO user = saveUser("credit_empty_user");
        String token = login(user.getId());

        mockMvc.perform(get("/app/users/credit-logs").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    void list_rejects_out_of_range_page_params() throws Exception {
        UserDO user = saveUser("credit_invalid_user");
        String token = login(user.getId());

        mockMvc.perform(get("/app/users/credit-logs").header("satoken", token).param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/app/users/credit-logs").header("satoken", token).param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/app/users/credit-logs").header("satoken", token).param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    private UserDO saveUser(String username) {
        LocalDateTime now = LocalDateTime.now();
        UserDO user = UserDO.builder()
                .username(username)
                .password(PASSWORD_HASH)
                .nickname("昵称_" + username)
                .creditScore(100)
                .walletBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .status("normal")
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        return user;
    }

    private void saveCreditLog(Long userId, Long orderId, int changeValue, String reason,
                               LocalDateTime createTime) {
        creditLogMapper.insert(CreditLogDO.builder()
                .userId(userId)
                .changeValue(changeValue)
                .reason(reason)
                .relatedType("order")
                .relatedId(orderId)
                .createTime(createTime)
                .updateTime(createTime)
                .build());
    }

    private String login(Long userId) throws Exception {
        MvcResult result = mockMvc.perform(get("/test/user/login").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getHeader("satoken");
    }
}
