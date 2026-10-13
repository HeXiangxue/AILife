package com.ailife.service.agent.review;

/**
 * 图像审核服务接口：可扩展接入不同第三方审核服务商
 */
public interface ImageReviewService {

  /**
   * 审核单张图片
   *
   * @param imageUrl 图片地址
   * @return 审核结果
   */
  ImageReviewResult review(String imageUrl);
}
