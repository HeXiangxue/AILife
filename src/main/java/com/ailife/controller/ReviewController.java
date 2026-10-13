package com.ailife.controller;

import com.ailife.common.Result;
import com.ailife.dto.ReviewDTO;
import com.ailife.entity.Review;
import com.ailife.security.UserContext;
import com.ailife.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  /**
   * 提交评价（触发内容审核）
   */
  @PostMapping
  public Result<Review> create(@Valid @RequestBody ReviewDTO dto) {
    return Result.success(reviewService.create(UserContext.userId(), dto));
  }

  /**
   * 查询某商家已通过的评价
   */
  @GetMapping
  public Result<List<Review>> list(@RequestParam Long merchantId) {
    return Result.success(reviewService.listByMerchant(merchantId));
  }
}
