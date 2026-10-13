package com.ailife.security;

import com.ailife.common.constants.RedisKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtUtil {

  private final StringRedisTemplate redisTemplate;

  @Value("${ailife.jwt.secret}")
  private String secret;

  @Value("${ailife.jwt.expire-hours}")
  private long expireHours;

  private SecretKey key;

  public JwtUtil(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostConstruct
  public void init() {
    // HS256 要求密钥长度 >= 32 字节
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * 签发 token，并写入 Redis（便于吊销与单点登录控制）
   */
  public String createToken(Long userId, String username, String role) {
    Date now = new Date();
    Date expireAt = new Date(now.getTime() + expireHours * 3600_000L);
    String token = Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("username", username)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expireAt)
        .signWith(key)
        .compact();
    try {
      redisTemplate.opsForValue()
          .set(RedisKeys.TOKEN_PREFIX + userId, token, expireHours, TimeUnit.HOURS);
    } catch (Exception e) {
      log.debug("Redis 不可用，跳过 token 缓存：{}", e.getMessage());
    }
    return token;
  }

  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  /**
   * 校验 token：签名 + Redis 一致性（Redis 异常时仅做签名校验）
   */
  public boolean isValid(String token) {
    try {
      Claims claims = parseToken(token);
      Long userId = Long.valueOf(claims.getSubject());
      try {
        String cached = redisTemplate.opsForValue().get(RedisKeys.TOKEN_PREFIX + userId);
        return token.equals(cached);
      } catch (Exception e) {
        log.debug("Redis 不可用，仅校验 JWT 签名：{}", e.getMessage());
        return true;
      }
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 吊销用户 token（用于退出登录）
   */
  public void revoke(Long userId) {
    try {
      redisTemplate.delete(RedisKeys.TOKEN_PREFIX + userId);
    } catch (Exception e) {
      log.debug("Redis 不可用，吊销失败：{}", e.getMessage());
    }
  }
}
