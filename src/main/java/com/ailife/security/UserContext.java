package com.ailife.security;

import lombok.Data;

/**
 * 当前登录用户上下文（线程级）
 */
public class UserContext {

  private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

  public static void set(LoginUser user) {
    HOLDER.set(user);
  }

  public static LoginUser get() {
    return HOLDER.get();
  }

  public static Long userId() {
    LoginUser user = HOLDER.get();
    return user == null ? null : user.getId();
  }

  public static String role() {
    LoginUser user = HOLDER.get();
    return user == null ? null : user.getRole();
  }

  public static String username() {
    LoginUser user = HOLDER.get();
    return user == null ? null : user.getUsername();
  }

  public static void clear() {
    HOLDER.remove();
  }

  @Data
  public static class LoginUser {
    private Long id;
    private String username;
    private String role;
  }
}
