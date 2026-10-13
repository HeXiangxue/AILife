package com.ailife.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

  @NotBlank(message = "用户名不能为空")
  @Size(min = 3, max = 20, message = "用户名长度为 3-20 个字符")
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 30, message = "密码长度为 6-30 个字符")
  private String password;

  @NotBlank(message = "昵称不能为空")
  private String nickname;
}
