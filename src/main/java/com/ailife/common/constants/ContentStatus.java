package com.ailife.common.constants;

/**
 * 内容审核状态（review / post 表 status 字段）
 */
public final class ContentStatus {

  /** 待审核 */
  public static final int PENDING = 0;
  /** 已通过 */
  public static final int APPROVED = 1;
  /** 已拒绝 */
  public static final int REJECTED = 2;
  /** 人工复审 */
  public static final int MANUAL = 3;

  private ContentStatus() {
  }
}
