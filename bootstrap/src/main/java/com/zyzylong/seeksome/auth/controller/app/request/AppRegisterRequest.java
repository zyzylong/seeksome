package com.zyzylong.seeksome.auth.controller.app.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求。
 */
@Schema(description = "用户注册请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRegisterRequest {

    @Schema(description = "用户名，全局唯一，首尾空白会被去除", example = "xiaoming")
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Schema(description = "登录密码，明文传输须使用 HTTPS", example = "Passw0rd!")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    @Schema(description = "昵称，首尾空白会被去除", example = "小明")
    @NotBlank
    @Size(max = 64)
    private String nickname;
}
