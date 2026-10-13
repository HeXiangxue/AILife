package com.ailife.controller;

import com.ailife.common.Result;
import com.ailife.dto.ChatRequest;
import com.ailife.dto.ChatResult;
import com.ailife.service.agent.QaAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentChatController {

  private final QaAgentService qaAgentService;

  /**
   * 智能问答：Agent 自主选择工具（商家搜索/评价查询/地理定位）并汇总回答
   */
  @PostMapping("/chat")
  public Result<ChatResult> chat(@Valid @RequestBody ChatRequest request) {
    String answer = qaAgentService.chat(request.getQuestion(), request.getLatitude(), request.getLongitude());
    ChatResult result = new ChatResult();
    result.setAnswer(answer);
    return Result.success(result);
  }
}
