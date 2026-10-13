package com.ailife.security;

import com.ailife.common.BizException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

  private final JwtUtil jwtUtil;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // 放行 CORS 预检请求
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      throw new BizException(401, "未登录或登录已过期");
    }
    String token = header.substring(7);
    if (!jwtUtil.isValid(token)) {
      throw new BizException(401, "未登录或登录已过期");
    }
    Claims claims = jwtUtil.parseToken(token);
    UserContext.LoginUser user = new UserContext.LoginUser();
    user.setId(Long.valueOf(claims.getSubject()));
    user.setUsername(claims.get("username", String.class));
    user.setRole(claims.get("role", String.class));
    UserContext.set(user);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    UserContext.clear();
  }
}
