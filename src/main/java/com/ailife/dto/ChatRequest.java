package com.ailife.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

  @NotBlank(message = "问题不能为空")
  private String question;

  /** 用户经度（可选，用于附近商家推荐） */
  private Double longitude;

  /** 用户纬度（可选） */
  private Double latitude;
}
