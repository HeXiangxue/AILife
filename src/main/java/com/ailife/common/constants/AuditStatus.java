package com.ailife.common.constants;

/**
 * 审核最终状态（audit_record.audit_status 字段）
 */
public enum AuditStatus {

  APPROVED("已通过"),
  REJECTED("已拒绝"),
  MANUAL("转人工复审");

  private final String desc;

  AuditStatus(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }
}
