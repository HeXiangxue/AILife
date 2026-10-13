package com.ailife.controller;

import com.ailife.common.Result;
import com.ailife.dto.PostDTO;
import com.ailife.entity.Post;
import com.ailife.security.UserContext;
import com.ailife.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  /**
   * 发布动态（触发内容审核）
   */
  @PostMapping
  public Result<Post> create(@Valid @RequestBody PostDTO dto) {
    return Result.success(postService.create(UserContext.userId(), dto));
  }

  /**
   * 按审核状态查询动态（默认展示已通过）
   */
  @GetMapping
  public Result<List<Post>> list(@RequestParam(defaultValue = "1") Integer status) {
    return Result.success(postService.listByStatus(status));
  }

  /**
   * 我的动态
   */
  @GetMapping("/mine")
  public Result<List<Post>> mine() {
    return Result.success(postService.listByUser(UserContext.userId()));
  }

  @GetMapping("/{id}")
  public Result<Post> detail(@PathVariable Long id) {
    return Result.success(postService.getById(id));
  }
}
