package com.ailife.service.agent.review;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LLM 语义审核：对文本内容进行安全分类，输出决策、置信度与风险类型。
 * 命中低置信度/解析失败场景时返回 UNCERTAIN，交由人工复审。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmContentReviewService {

  private static final String AUDIT_MARKER = "__AUDIT_TASK__";

  private static final String PROMPT_TEMPLATE = """
      你是内容审核助手，请对以下用户发布的内容进行安全审核。
      %s
      请判断内容是否存在以下违规情形：涉政敏感、色情低俗、暴力血腥、赌博、广告导流、辱骂攻击、诈骗信息。
      内容如下：
      --------------------------------
      %s
      --------------------------------
      请只输出一个 JSON 对象，不要输出任何其他内容，格式如下：
      {"decision":"PASS或REJECT或UNCERTAIN","confidence":0到1之间的小数,"riskType":"NONE或POLITICS或PORN或VIOLENCE或GAMBLE或AD或INSULT或FRAUD","reason":"简要说明"}
      """;

  private final ChatModel chatModel;
  private final ObjectMapper objectMapper;

  public LlmReviewResult review(String text) {
    String prompt = PROMPT_TEMPLATE.formatted(AUDIT_MARKER, text);
    String response;
    try {
      response = chatModel.chat(ChatRequest.builder()
              .messages(List.of(UserMessage.from(prompt)))
              .build())
          .aiMessage()
          .text();
    } catch (Exception e) {
      log.warn("LLM 审核调用异常，转人工复审：{}", e.getMessage());
      LlmReviewResult failed = new LlmReviewResult();
      failed.setDecision("UNCERTAIN");
      failed.setConfidence(0);
      failed.setRiskType("UNKNOWN");
      failed.setReason("LLM 审核服务异常");
      return failed;
    }
    try {
      JsonNode node = objectMapper.readTree(extractJson(response));
      LlmReviewResult result = new LlmReviewResult();
      result.setDecision(node.path("decision").asText("UNCERTAIN"));
      result.setConfidence(node.path("confidence").asDouble(0.5));
      result.setRiskType(node.path("riskType").asText("UNKNOWN"));
      result.setReason(node.path("reason").asText(response));
      return result;
    } catch (Exception e) {
      log.warn("LLM 审核结果解析失败，转人工复审，原始输出：{}", response);
      LlmReviewResult result = new LlmReviewResult();
      result.setDecision("UNCERTAIN");
      result.setConfidence(0);
      result.setRiskType("UNKNOWN");
      result.setReason("LLM 输出解析失败");
      return result;
    }
  }

  private String extractJson(String text) {
    int start = text.indexOf('{');
    int end = text.lastIndexOf('}');
    if (start >= 0 && end > start) {
      return text.substring(start, end + 1);
    }
    return text;
  }
}
