package com.ailife.common.constants;

/**
 * Redis Key 常量
 */
public final class RedisKeys {

  /** 登录 token 缓存前缀，值为用户当前有效的 JWT */
  public static final String TOKEN_PREFIX = "ai:life:token:";

  private RedisKeys() {
  }
}
