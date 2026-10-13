package com.ailife.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商家查询条件（多条件组合）
 */
@Data
public class MerchantQuery {

  /** 分类 */
  private String category;
  /** 关键词（匹配名称或简介） */
  private String keyword;
  /** 最低评分 */
  private BigDecimal minRating;
  /** 最高人均价格 */
  private BigDecimal maxPrice;
  /** 用户经度（可选，提供后按距离排序） */
  private BigDecimal longitude;
  /** 用户纬度 */
  private BigDecimal latitude;
  /** 返回数量 */
  private Integer limit;
  /** 最大距离（公里），仅附近搜索使用 */
  private Double maxDistance;
}
