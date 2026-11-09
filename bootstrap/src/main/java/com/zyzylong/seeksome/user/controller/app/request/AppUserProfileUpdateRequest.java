package com.zyzylong.seeksome.user.controller.app.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个人资料编辑请求。
 */
@Schema(description = "个人资料编辑请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserProfileUpdateRequest {

    @Schema(description = "昵称，首尾空白会被去除", example = "小明")
    @NotBlank
    @Size(max = 64)
    private String nickname;

    @Schema(description = "头像地址；不传保持原头像，传空字符串清空头像",
            example = "/files/avatar/xiaoming.png")
    @Size(max = 255)
    private String avatar;
}
