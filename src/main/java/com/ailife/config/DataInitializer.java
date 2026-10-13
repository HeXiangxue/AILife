package com.ailife.config;

import com.ailife.entity.SysUser;
import com.ailife.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化演示账号（密码为 bcrypt 密文，无法直接在 SQL 中预设）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final SysUserMapper userMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public void run(String... args) {
    createIfAbsent("admin", "admin123", "管理员", "ADMIN", 100);
    createIfAbsent("user1", "user123", "小美", "USER", 90);
    // 低信誉用户，用于演示信誉校验升级为人工复审
    createIfAbsent("user2", "user123", "小强", "USER", 50);
    log.info("演示账号初始化完成：admin/admin123、user1/user123、user2/user123");
  }

  private void createIfAbsent(String username, String rawPassword, String nickname, String role, int reputation) {
    if (userMapper.selectByUsername(username) != null) {
      return;
    }
    SysUser user = new SysUser();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setNickname(nickname);
    user.setRole(role);
    user.setStatus(1);
    user.setReputationScore(reputation);
    userMapper.insert(user);
  }
}
