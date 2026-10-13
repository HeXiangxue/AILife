package com.ailife.service;

import com.ailife.common.constants.ContentStatus;
import com.ailife.dto.ReviewDTO;
import com.ailife.entity.Review;
import com.ailife.mapper.ReviewMapper;
import com.ailife.service.agent.ReviewAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

  private final ReviewMapper reviewMapper;
  private final MerchantService merchantService;
  private final ReviewAgentService reviewAgentService;

  /**
   * 提交评价：入库后立即触发内容审核 Agent
   */
  public Review create(Long userId, ReviewDTO dto) {
    merchantService.getById(dto.getMerchantId());
    Review review = new Review();
    review.setMerchantId(dto.getMerchantId());
    review.setUserId(userId);
    review.setContent(dto.getContent());
    review.setRating(dto.getRating());
    review.setImages(dto.getImages() == null ? null : String.join(",", dto.getImages()));
    review.setStatus(ContentStatus.PENDING);
    reviewMapper.insert(review);
    reviewAgentService.auditReview(review.getId(), userId);
    return reviewMapper.selectById(review.getId());
  }

  public List<Review> listByMerchant(Long merchantId) {
    return reviewMapper.selectApprovedByMerchant(merchantId);
  }
}
