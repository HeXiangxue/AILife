package com.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditRecord {

  private Long id;
  /** 目标类型：REVIEW / POST */
  private String targetType;
  private Long targetId;
  private String textSensitiveWords;
  private String imageResult;
  private String reputationResult;
  private String llmResult;
  /** 风险等级：LOW / MEDIUM / HIGH */
  private String riskLevel;
  /** 审核状态：APPROVED / REJECTED / MANUAL */
  private String auditStatus;
  private String reviewComment;
  private Long reviewerId;
  private LocalDateTime createTime;
  private LocalDateTime reviewTime;
}
