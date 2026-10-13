package com.ailife.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {

  private Long id;
  private Long userId;
  private String title;
  private String content;
  /** 图片 URL，逗号分隔 */
  private String images;
  /** 审核状态：0待审核 1通过 2拒绝 3人工复审 */
  private Integer status;
  private Long auditId;
  private LocalDateTime createTime;
}
