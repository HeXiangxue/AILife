package com.ailife.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Merchant {

  private Long id;
  private String name;
  private String category;
  private String address;
  private BigDecimal longitude;
  private BigDecimal latitude;
  private String phone;
  private BigDecimal rating;
  private BigDecimal avgPrice;
  private String openHours;
  private String description;
  private Integer status;
  /** 查询时计算的与用户的距离（公里），非数据库字段 */
  private Double distanceKm;
  private LocalDateTime createTime;
}
