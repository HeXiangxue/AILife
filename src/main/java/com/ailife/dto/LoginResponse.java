package com.ailife.dto;

import com.ailife.entity.SysUser;
import lombok.Data;

@Data
public class LoginResponse {

  private String token;
  private SysUser user;
}
