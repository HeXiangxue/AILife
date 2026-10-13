package com.ailife.controller;

import com.ailife.common.Result;
import com.ailife.dto.LoginRequest;
import com.ailife.dto.LoginResponse;
import com.ailife.dto.RegisterRequest;
import com.ailife.entity.SysUser;
import com.ailife.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
    return Result.success(authService.register(request));
  }

  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return Result.success(authService.login(request));
  }

  @PostMapping("/logout")
  public Result<Void> logout() {
    authService.logout();
    return Result.success();
  }

  @GetMapping("/me")
  public Result<SysUser> me() {
    return Result.success(authService.me());
  }
}
