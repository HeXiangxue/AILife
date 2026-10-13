package com.ailife.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewDTO {

  @NotNull(message = "商家不能为空")
  private Long merchantId;

  @NotBlank(message = "评价内容不能为空")
  private String content;

  @Min(value = 1, message = "评分为 1-5")
  @Max(value = 5, message = "评分为 1-5")
  private Integer rating;

  private List<String> images;
}
