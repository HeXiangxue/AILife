package com.ailife.config;

import com.ailife.service.agent.mock.MockChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型配置：优先使用通义千问 DashScope（OpenAI 兼容模式），
 * 未配置 API Key 时降级为本地 Mock 模型。
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

  @Value("${ailife.dashscope.api-key:}")
  private String apiKey;

  @Value("${ailife.dashscope.base-url}")
  private String baseUrl;

  @Value("${ailife.dashscope.model}")
  private String modelName;

  @Bean
  public ChatModel chatModel(ObjectProvider<MockChatModel> mockProvider) {
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("未配置 DASHSCOPE_API_KEY，使用本地 Mock 模型（仅演示工具调用与审核流程，无真实理解能力）");
      return mockProvider.getIfAvailable();
    }
    log.info("使用通义千问 DashScope 模型：{}，base-url：{}", modelName, baseUrl);
    return OpenAiChatModel.builder()
        .baseUrl(baseUrl)
        .apiKey(apiKey)
        .modelName(modelName)
        .temperature(0.7)
        .logRequests(true)
        .logResponses(true)
        .build();
  }
}
