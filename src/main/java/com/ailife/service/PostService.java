package com.ailife.service;

import com.ailife.common.BizException;
import com.ailife.common.constants.ContentStatus;
import com.ailife.dto.PostDTO;
import com.ailife.entity.Post;
import com.ailife.mapper.PostMapper;
import com.ailife.service.agent.ReviewAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostMapper postMapper;
  private final ReviewAgentService reviewAgentService;

  /**
   * 发布动态：入库后立即触发内容审核 Agent
   */
  public Post create(Long userId, PostDTO dto) {
    Post post = new Post();
    post.setUserId(userId);
    post.setTitle(dto.getTitle());
    post.setContent(dto.getContent());
    post.setImages(dto.getImages() == null ? null : String.join(",", dto.getImages()));
    post.setStatus(ContentStatus.PENDING);
    postMapper.insert(post);
    reviewAgentService.auditPost(post.getId(), userId);
    return postMapper.selectById(post.getId());
  }

  public List<Post> listByStatus(Integer status) {
    return postMapper.selectByStatus(status);
  }

  public List<Post> listByUser(Long userId) {
    return postMapper.selectByUser(userId);
  }

  public Post getById(Long id) {
    Post post = postMapper.selectById(id);
    if (post == null) {
      throw new BizException(404, "动态不存在");
    }
    return post;
  }
}
