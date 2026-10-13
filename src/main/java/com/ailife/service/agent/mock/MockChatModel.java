package com.ailife.service.agent.mock;

import com.ailife.entity.Merchant;
import com.ailife.mapper.MerchantMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本地 Mock 大模型：
 * 当未配置 DASHSCOPE_API_KEY 时兜底使用，用于离线演示 Agent 工具调用与内容审核流程。
 * 该模型根据关键词模拟工具选择，不具备真实理解能力，仅保证全流程可跑通。
 */
@Slf4j
@Component
public class MockChatModel implements ChatModel {

  private static final String AUDIT_MARKER = "__AUDIT_TASK__";

  private final MerchantMapper merchantMapper;

  public MockChatModel(MerchantMapper merchantMapper) {
    this.merchantMapper = merchantMapper;
  }

  @Override
  public ChatResponse doChat(ChatRequest chatRequest) {
    List<ChatMessage> messages = chatRequest.messages();
    String lastUserText = findLastUserText(messages);

    // 1) 内容审核任务：返回固定审核结果
    if (lastUserText != null && lastUserText.contains(AUDIT_MARKER)) {
      String auditResult = "{\"decision\":\"PASS\",\"confidence\":0.95,\"riskType\":\"NONE\",\"reason\":\"内容正常，无明显违规信息\"}";
      log.info("[Mock] 内容审核返回：{}", auditResult);
      return ChatResponse.builder().aiMessage(AiMessage.from(auditResult)).build();
    }

    // 2) 若上一轮已执行工具，则对工具结果进行汇总回复
    ToolExecutionResultMessage lastResult = findLastToolResult(messages);
    if (lastResult != null) {
      String summary = "好的，为您整理结果如下：\n" + lastResult.text();
      log.info("[Mock] 对工具结果 {} 进行汇总回复", lastResult.toolName());
      return ChatResponse.builder().aiMessage(AiMessage.from(summary)).build();
    }

    // 3) 首轮：根据关键词模拟 Agent 自主选择工具
    ToolExecutionRequest request = decideToolRequest(lastUserText);
    if (request != null) {
      log.info("[Mock] 模拟 Agent 选择工具：{}，参数：{}", request.name(), request.arguments());
      return ChatResponse.builder().aiMessage(AiMessage.from(List.of(request))).build();
    }

    // 4) 兜底回复
    return ChatResponse.builder()
        .aiMessage(AiMessage.from("我暂时无法理解您的需求。您可以试试这样问：\n- 推荐附近的火锅店\n- 全聚德烤鸭店的评价怎么样\n- 人均 50 元以内的餐厅"))
        .build();
  }

  /**
   * 根据用户输入关键词模拟工具选择
   */
  private ToolExecutionRequest decideToolRequest(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    // 评价类问题 → 评价查询工具
    if (containsAny(text, "评价", "口碑", "评论", "怎么样")) {
      String name = extractMerchantName(text);
      if (name != null) {
        return buildToolRequest("queryMerchantReviews", "{\"merchantName\":\"" + name + "\",\"limit\":3}");
      }
    }
    // 附近类问题 → 附近商家工具（默认北京坐标）
    if (containsAny(text, "附近", "最近", "距离", "周边")) {
      return buildToolRequest("findNearbyMerchants",
          "{\"longitude\":116.397428,\"latitude\":39.90923,\"maxDistance\":5,\"limit\":5}");
    }
    // 分类/关键词类问题 → 多条件商家搜索工具
    String category = extractCategory(text);
    if (category != null) {
      return buildToolRequest("searchMerchant", "{\"category\":\"" + category + "\",\"limit\":5}");
    }
    if (extractKeyword(text) != null) {
      return buildToolRequest("searchMerchant", "{\"keyword\":\"" + extractKeyword(text) + "\",\"limit\":5}");
    }
    return null;
  }

  private boolean containsAny(String text, String... keys) {
    for (String key : keys) {
      if (text.contains(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 从文本中识别商家分类关键词
   */
  private String extractCategory(String text) {
    String[] categories = {"火锅", "烧烤", "川菜", "咖啡", "奶茶", "甜品", "餐厅", "日料", "快餐", "便利店", "书店"};
    for (String c : categories) {
      if (text.contains(c)) {
        return c;
      }
    }
    return null;
  }

  /**
   * 从文本中识别商家名称（基于数据库中的商家）
   */
  private String extractMerchantName(String text) {
    List<Merchant> merchants = merchantMapper.selectIdAndName();
    for (Merchant m : merchants) {
      if (m.getName() != null && text.contains(m.getName())) {
        return m.getName();
      }
    }
    return null;
  }

  /**
   * 识别关键词：截取短文本作为搜索关键词
   */
  private String extractKeyword(String text) {
    if (text.length() <= 6) {
      return text.trim();
    }
    return null;
  }

  private ToolExecutionRequest buildToolRequest(String name, String arguments) {
    return ToolExecutionRequest.builder()
        .id("mock_" + name)
        .name(name)
        .arguments(arguments)
        .build();
  }

  private String findLastUserText(List<ChatMessage> messages) {
    for (int i = messages.size() - 1; i >= 0; i--) {
      if (messages.get(i) instanceof UserMessage m) {
        return m.singleText();
      }
    }
    return null;
  }

  private ToolExecutionResultMessage findLastToolResult(List<ChatMessage> messages) {
    for (int i = messages.size() - 1; i >= 0; i--) {
      if (messages.get(i) instanceof ToolExecutionResultMessage m) {
        return m;
      }
    }
    return null;
  }
}
