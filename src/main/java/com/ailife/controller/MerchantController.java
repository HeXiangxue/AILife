package com.ailife.controller;

import com.ailife.common.BizException;
import com.ailife.common.Result;
import com.ailife.dto.MerchantQuery;
import com.ailife.entity.Merchant;
import com.ailife.security.UserContext;
import com.ailife.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

  private final MerchantService merchantService;

  /**
   * 多条件查询商家列表
   */
  @GetMapping
  public Result<List<Merchant>> list(MerchantQuery query) {
    return Result.success(merchantService.search(query));
  }

  @GetMapping("/categories")
  public Result<List<String>> categories() {
    return Result.success(merchantService.categories());
  }

  @GetMapping("/{id}")
  public Result<Merchant> detail(@PathVariable Long id) {
    return Result.success(merchantService.getById(id));
  }

  /**
   * 新增商家（仅管理员）
   */
  @PostMapping
  public Result<Merchant> create(@RequestBody Merchant merchant) {
    if (!"ADMIN".equals(UserContext.role())) {
      throw new BizException(403, "仅管理员可创建商家");
    }
    return Result.success(merchantService.create(merchant));
  }
}
