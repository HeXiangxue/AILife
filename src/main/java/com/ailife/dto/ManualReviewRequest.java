package com.ailife.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManualReviewRequest {

  /** 复审决策：APPROVED / REJECTED */
  @NotBlank(message = "复审决策不能为空")
  private String decision;

  private String comment;
}
