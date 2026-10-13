package com.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUser {

  private Long id;
  private String username;
  private String password;
  private String nickname;
  private String phone;
  private String avatar;
  private Integer reputationScore;
  private String role;
  private Integer status;
  private LocalDateTime createTime;
}
