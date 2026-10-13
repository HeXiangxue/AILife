package com.ailife.dto;

import com.ailife.entity.AuditRecord;
import lombok.Data;

/**
 * 审核详情视图（审核记录 + 目标内容快照，便于人工复审展示）
 */
@Data
public class AuditVO {

  private AuditRecord record;
  /** 目标标题（POST 有） */
  private String targetTitle;
  /** 目标正文 */
  private String targetContent;
  /** 目标作者昵称 */
  private String authorName;
}
