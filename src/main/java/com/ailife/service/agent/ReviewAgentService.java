package com.ailife.service.agent;

import com.ailife.common.BizException;
import com.ailife.common.constants.AuditStatus;
import com.ailife.common.constants.ContentStatus;
import com.ailife.common.constants.RiskLevel;
import com.ailife.entity.AuditRecord;
import com.ailife.entity.Post;
import com.ailife.entity.Review;
import com.ailife.entity.SysUser;
import com.ailife.mapper.AuditRecordMapper;
import com.ailife.mapper.PostMapper;
import com.ailife.mapper.ReviewMapper;
import com.ailife.mapper.SysUserMapper;
import com.ailife.service.agent.review.ImageReviewResult;
import com.ailife.service.agent.review.ImageReviewService;
import com.ailife.service.agent.review.LlmContentReviewService;
import com.ailife.service.agent.review.LlmReviewResult;
import com.ailife.service.agent.review.SensitiveWordFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容审核 Agent：
 * 将「文本敏感词 -> 图像审核 -> 用户信誉校验 -> LLM 语义审核」串联为审核流程，
 * 对模型输出进行结果校验，命中不确定场景时转人工复审，审核结果落库并更新内容状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAgentService {

  private final SensitiveWordFilter sensitiveWordFilter;
  private final ImageReviewService imageReviewService;
  private final LlmContentReviewService llmContentReviewService;
  private final SysUserMapper sysUserMapper;
  private final ReviewMapper reviewMapper;
  private final PostMapper postMapper;
  private final AuditRecordMapper auditRecordMapper;
  private final ObjectMapper objectMapper;

  @Value("${ailife.review.reputation-low-threshold:60}")
  private int reputationLowThreshold;

  @Value("${ailife.review.manual-confidence-threshold:0.8}")
  private double manualConfidenceThreshold;

  /**
   * 评价审核入口
   */
  public AuditRecord auditReview(Long reviewId, Long userId) {
    Review review = reviewMapper.selectById(reviewId);
    if (review == null) {
      throw new BizException(404, "评价不存在");
    }
    return doAudit("REVIEW", reviewId, userId, review.getContent(), splitImages(review.getImages()));
  }

  /**
   * 动态审核入口
   */
  public AuditRecord auditPost(Long postId, Long userId) {
    Post post = postMapper.selectById(postId);
    if (post == null) {
      throw new BizException(404, "动态不存在");
    }
    String text = (post.getTitle() == null ? "" : post.getTitle()) + "\n" + post.getContent();
    return doAudit("POST", postId, userId, text, splitImages(post.getImages()));
  }

  /**
   * 审核主流程
   */
  private AuditRecord doAudit(String targetType, Long targetId, Long userId, String text, List<String> images) {
    // 1) 文本敏感词检测
    List<String> sensitiveWords = sensitiveWordFilter.findSensitiveWords(text);

    // 2) 图像审核（逐张）
    List<ImageReviewResult> imageResults = new ArrayList<>();
    for (String url : images) {
      imageResults.add(imageReviewService.review(url));
    }

    // 3) 用户信誉校验
    SysUser user = sysUserMapper.selectById(userId);
    int reputation = (user != null && user.getReputationScore() != null) ? user.getReputationScore() : 100;
    String reputationResult = "用户信誉分：" + reputation + "（昵称：" + (user == null ? "未知" : user.getNickname()) + "）";

    // 4) LLM 语义审核
    LlmReviewResult llmResult = llmContentReviewService.review(text);

    // 5) 综合决策与结果校验
    Decision decision = decide(sensitiveWords, imageResults, reputation, llmResult);

    // 6) 审核结果落库
    AuditRecord record = new AuditRecord();
    record.setTargetType(targetType);
    record.setTargetId(targetId);
    record.setTextSensitiveWords(sensitiveWords.isEmpty() ? "无" : String.join("、", sensitiveWords));
    record.setImageResult(imageResults.isEmpty() ? "无图片" : imageResults.stream()
        .map(r -> r.getUrl() + ":" + r.getDecision() + "(" + r.getMessage() + ")")
        .collect(Collectors.joining(";")));
    record.setReputationResult(reputationResult);
    record.setLlmResult(toJson(llmResult));
    record.setRiskLevel(decision.riskLevel.name());
    record.setAuditStatus(decision.auditStatus.name());
    auditRecordMapper.insert(record);

    // 7) 更新内容审核状态
    updateContentStatus(targetType, targetId, record.getId(), decision.auditStatus);

    log.info("内容审核完成 targetType={} targetId={} status={} risk={} 敏感词={} 图像={} 信誉分={} LLM={}",
        targetType, targetId, decision.auditStatus, decision.riskLevel,
        record.getTextSensitiveWords(), record.getImageResult(), reputation, record.getLlmResult());
    return record;
  }

  /**
   * 综合决策：
   * - 命中敏感词或图像违规 -> 拒绝（高风险）
   * - 图像疑似/未配置 -> 转人工复审
   * - LLM 判定违规 -> 拒绝
   * - LLM 通过且置信度高 -> 通过
   * - LLM 不确定或置信度低 -> 转人工复审
   * - 低信誉用户 -> 通过升级为人工复审
   */
  private Decision decide(List<String> sensitiveWords, List<ImageReviewResult> imageResults,
      int reputation, LlmReviewResult llmResult) {
    // 强规则：敏感词 / 图像违规直接拒绝
    if (!sensitiveWords.isEmpty()) {
      return new Decision(AuditStatus.REJECTED, RiskLevel.HIGH, "命中敏感词：" + sensitiveWords);
    }
    boolean imageRejected = imageResults.stream().anyMatch(r -> ImageReviewResult.REJECT.equals(r.getDecision()));
    if (imageRejected) {
      return new Decision(AuditStatus.REJECTED, RiskLevel.HIGH, "图像命中违规内容");
    }
    boolean imageUncertain = imageResults.stream().anyMatch(r -> ImageReviewResult.UNCERTAIN.equals(r.getDecision()));
    if (imageUncertain) {
      return new Decision(AuditStatus.MANUAL, RiskLevel.MEDIUM, "图像疑似违规或审核未配置，转人工复审");
    }

    // LLM 语义审核
    if ("REJECT".equals(llmResult.getDecision())) {
      return new Decision(AuditStatus.REJECTED, RiskLevel.HIGH, "LLM 判定违规：" + llmResult.getReason());
    }
    if ("PASS".equals(llmResult.getDecision()) && llmResult.getConfidence() >= manualConfidenceThreshold) {
      if (reputation < reputationLowThreshold) {
        return new Decision(AuditStatus.MANUAL, RiskLevel.MEDIUM,
            "LLM 判定通过但用户信誉分偏低（" + reputation + "），转人工复审");
      }
      return new Decision(AuditStatus.APPROVED, RiskLevel.LOW, "LLM 判定通过，置信度 " + llmResult.getConfidence());
    }
    // 不确定 / 置信度低
    return new Decision(AuditStatus.MANUAL, RiskLevel.MEDIUM,
        "LLM 判定不确定（置信度 " + llmResult.getConfidence() + "），转人工复审");
  }

  /**
   * 人工复审：更新审核记录并同步内容状态
   */
  public AuditRecord manualReview(Long auditId, String decision, String comment, Long reviewerId) {
    AuditRecord record = auditRecordMapper.selectById(auditId);
    if (record == null) {
      throw new BizException(404, "审核记录不存在");
    }
    if (!"APPROVED".equalsIgnoreCase(decision) && !"REJECTED".equalsIgnoreCase(decision)) {
      throw new BizException(400, "复审决策仅支持 APPROVED 或 REJECTED");
    }
    String status = "APPROVED".equalsIgnoreCase(decision) ? AuditStatus.APPROVED.name() : AuditStatus.REJECTED.name();
    auditRecordMapper.updateReview(auditId, status, comment, reviewerId);
    updateContentStatus(record.getTargetType(), record.getTargetId(), auditId, AuditStatus.valueOf(status));
    log.info("人工复审完成 auditId={} decision={} reviewerId={}", auditId, status, reviewerId);
    return auditRecordMapper.selectById(auditId);
  }

  private void updateContentStatus(String targetType, Long targetId, Long auditId, AuditStatus auditStatus) {
    int status = switch (auditStatus) {
      case APPROVED -> ContentStatus.APPROVED;
      case REJECTED -> ContentStatus.REJECTED;
      case MANUAL -> ContentStatus.MANUAL;
    };
    if ("REVIEW".equals(targetType)) {
      reviewMapper.updateAuditResult(targetId, status, auditId);
    } else if ("POST".equals(targetType)) {
      postMapper.updateAuditResult(targetId, status, auditId);
    }
  }

  private List<String> splitImages(String images) {
    if (images == null || images.isBlank()) {
      return List.of();
    }
    return Arrays.stream(images.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      return obj == null ? "null" : obj.toString();
    }
  }

  /**
   * 决策结果
   */
  private record Decision(AuditStatus auditStatus, RiskLevel riskLevel, String reason) {
  }
}
