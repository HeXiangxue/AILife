package com.ailife.controller;

import com.ailife.common.BizException;
import com.ailife.common.Result;
import com.ailife.dto.AuditVO;
import com.ailife.dto.ManualReviewRequest;
import com.ailife.entity.AuditRecord;
import com.ailife.entity.Post;
import com.ailife.entity.Review;
import com.ailife.entity.SysUser;
import com.ailife.mapper.AuditRecordMapper;
import com.ailife.mapper.PostMapper;
import com.ailife.mapper.ReviewMapper;
import com.ailife.mapper.SysUserMapper;
import com.ailife.security.UserContext;
import com.ailife.service.agent.ReviewAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 审核工作台（仅管理员）：查看审核记录、人工复审
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AuditRecordMapper auditRecordMapper;
  private final ReviewMapper reviewMapper;
  private final PostMapper postMapper;
  private final SysUserMapper sysUserMapper;
  private final ReviewAgentService reviewAgentService;

  /**
   * 审核记录列表（可按状态筛选）
   */
  @GetMapping("/audits")
  public Result<List<AuditVO>> audits(@RequestParam(required = false) String status) {
    requireAdmin();
    List<AuditRecord> records = (status == null || status.isBlank())
        ? auditRecordMapper.selectAll()
        : auditRecordMapper.selectByStatus(status);
    return Result.success(toVO(records));
  }

  @GetMapping("/audits/{id}")
  public Result<AuditVO> auditDetail(@PathVariable Long id) {
    requireAdmin();
    AuditRecord record = auditRecordMapper.selectById(id);
    if (record == null) {
      throw new BizException(404, "审核记录不存在");
    }
    return Result.success(toVO(List.of(record)).get(0));
  }

  /**
   * 人工复审：通过 / 拒绝
   */
  @PostMapping("/audits/{id}/review")
  public Result<AuditVO> review(@PathVariable Long id, @Valid @RequestBody ManualReviewRequest request) {
    requireAdmin();
    AuditRecord record = reviewAgentService.manualReview(id, request.getDecision(), request.getComment(), UserContext.userId());
    return Result.success(toVO(List.of(record)).get(0));
  }

  private void requireAdmin() {
    if (!"ADMIN".equals(UserContext.role())) {
      throw new BizException(403, "仅管理员可操作审核工作台");
    }
  }

  private List<AuditVO> toVO(List<AuditRecord> records) {
    List<AuditVO> list = new ArrayList<>();
    for (AuditRecord record : records) {
      AuditVO vo = new AuditVO();
      vo.setRecord(record);
      if ("REVIEW".equals(record.getTargetType())) {
        Review review = reviewMapper.selectById(record.getTargetId());
        if (review != null) {
          vo.setTargetContent(review.getContent());
          vo.setAuthorName(userNickname(review.getUserId()));
        }
      } else if ("POST".equals(record.getTargetType())) {
        Post post = postMapper.selectById(record.getTargetId());
        if (post != null) {
          vo.setTargetTitle(post.getTitle());
          vo.setTargetContent(post.getContent());
          vo.setAuthorName(userNickname(post.getUserId()));
        }
      }
      list.add(vo);
    }
    return list;
  }

  private String userNickname(Long userId) {
    SysUser user = sysUserMapper.selectById(userId);
    return user == null ? "未知用户" : user.getNickname();
  }
}
