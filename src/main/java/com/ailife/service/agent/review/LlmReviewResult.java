package com.ailife.service.agent.review;

import lombok.Data;

/**
 * LLM 语义审核输出
 */
@Data
public class LlmReviewResult {

  /** PASS / REJECT / UNCERTAIN */
  private String decision;

  /** 置信度 0-1 */
  private double confidence;

  /** NONE / POLITICS / PORN / VIOLENCE / GAMBLE / AD / INSULT / FRAUD / UNKNOWN */
  private String riskType;

  private String reason;
}
