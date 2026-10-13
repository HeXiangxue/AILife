package com.ailife.common.constants;

/**
 * 风险等级
 */
public enum RiskLevel {

  LOW("低风险"),
  MEDIUM("中风险"),
  HIGH("高风险");

  private final String desc;

  RiskLevel(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
