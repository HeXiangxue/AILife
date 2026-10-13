package com.ailife.service.agent;

import com.ailife.service.agent.tools.GeoLocationTool;
import com.ailife.service.agent.tools.MerchantSearchTool;
import com.ailife.service.agent.tools.ReviewSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 智能问答 Agent：
 * 基于 LangChain4j AiServices 构建，将商家搜索、评价查询、地理定位封装为 Tool，
 * 由 Agent 自主选择工具并汇总结果，支持多条件商家推荐。
 */
@Slf4j
@Service
public class QaAgentService {

  private static final String SYSTEM_PROMPT = """
      你是一位本地生活智能助手「AI 生活优选」，帮助用户发现优质商家。
      你可以使用以下工具来回答用户问题：
      1. searchMerchant：按分类、关键词、评分、人均价格等多条件组合搜索商家
      2. queryMerchantReviews：查询某个商家的用户评价与口碑
      3. findNearbyMerchants：根据用户经纬度查找附近商家
      规则：
      - 先分析用户意图，选择合适的工具；必要时可以组合调用多个工具。
      - 基于工具返回的数据组织回答，使用简洁、口语化的中文。
      - 回答中尽量包含商家名称、分类、评分、人均价格、距离等信息，方便用户做选择。
      - 用户提供位置时优先使用 findNearbyMerchants 或传入经纬度做距离排序。
      - 如果工具没有返回结果，如实告知并建议更换条件，不要编造信息。
      """;

  private final QaAssistant assistant;

  public QaAgentService(ChatModel chatModel,
      MerchantSearchTool merchantSearchTool,
      ReviewSearchTool reviewSearchTool,
      GeoLocationTool geoLocationTool) {
    this.assistant = AiServices.builder(QaAssistant.class)
        .chatModel(chatModel)
        .systemMessageProvider(chatMemoryId -> SYSTEM_PROMPT)
        .tools(merchantSearchTool, reviewSearchTool, geoLocationTool)
        .build();
  }

  /**
   * 提问：返回 Agent 汇总后的回答
   */
  public String chat(String question, Double latitude, Double longitude) {
    StringBuilder message = new StringBuilder(question);
    if (latitude != null && longitude != null) {
      message.append("\n（我的位置：经度").append(longitude)
          .append("，纬度").append(latitude).append("）");
    }
    try {
      return assistant.chat(message.toString());
    } catch (Exception e) {
      log.error("Agent 问答调用失败：{}", e.getMessage(), e);
      return "抱歉，模型服务暂时不可用，请检查 DASHSCOPE_API_KEY 配置或稍后再试。";
    }
  }

  /**
   * Agent 接口定义
   */
  interface QaAssistant {
    String chat(String userMessage);
  }
}
