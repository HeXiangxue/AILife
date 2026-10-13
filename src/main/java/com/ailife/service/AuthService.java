package com.ailife.service;

import com.ailife.common.BizException;
import com.ailife.dto.LoginRequest;
import com.ailife.dto.LoginResponse;
import com.ailife.dto.RegisterRequest;
import com.ailife.entity.SysUser;
import com.ailife.mapper.SysUserMapper;
import com.ailife.security.JwtUtil;
import com.ailife.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final SysUserMapper userMapper;
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public LoginResponse register(RegisterRequest request) {
    if (userMapper.selectByUsername(request.getUsername()) != null) {
      throw new BizException(400, "用户名已存在");
    }
    SysUser user = new SysUser();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setNickname(request.getNickname());
    user.setRole("USER");
    user.setStatus(1);
    user.setReputationScore(100);
    userMapper.insert(user);
    return buildLoginResponse(user);
  }

  public LoginResponse login(LoginRequest request) {
    SysUser user = userMapper.selectByUsername(request.getUsername());
    if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BizException(401, "用户名或密码错误");
    }
    if (user.getStatus() != null && user.getStatus() == 0) {
      throw new BizException(403, "账号已被禁用");
    }
    return buildLoginResponse(user);
  }

  public void logout() {
    Long userId = UserContext.userId();
    if (userId != null) {
      jwtUtil.revoke(userId);
    }
  }

  public SysUser me() {
    return userMapper.selectById(UserContext.userId());
  }

  private LoginResponse buildLoginResponse(SysUser user) {
    String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
    LoginResponse response = new LoginResponse();
    response.setToken(token);
    response.setUser(user);
    return response;
  }
}
