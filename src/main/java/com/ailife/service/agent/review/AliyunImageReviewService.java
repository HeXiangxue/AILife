package com.ailife.service.agent.review;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 阿里云内容安全 - 图像审核（真实第三方 API 接入，ImageModeration 2020-07-13）
 * 通过 CommonRequest 调用，未配置 AK/SK 时自动降级：返回 UNCERTAIN，交由人工复审兜底。
 */
@Slf4j
@Component
public class AliyunImageReviewService implements ImageReviewService {

  @Value("${ailife.image-review.enabled:true}")
  private boolean enabled;

  @Value("${ailife.image-review.aliyun.access-key-id:}")
  private String accessKeyId;

  @Value("${ailife.image-review.aliyun.access-key-secret:}")
  private String accessKeySecret;

  @Value("${ailife.image-review.aliyun.region-id:cn-shanghai}")
  private String regionId;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private volatile DefaultAcsClient client;

  @PostConstruct
  public void init() {
    if (enabled && StringUtils.hasText(accessKeyId) && StringUtils.hasText(accessKeySecret)) {
      try {
        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        client = new DefaultAcsClient(profile);
        log.info("阿里云内容安全图像审核初始化成功，region：{}", regionId);
      } catch (Exception e) {
        log.error("阿里云内容安全初始化失败，将降级为人工复审兜底", e);
      }
    } else {
      log.warn("未配置阿里云内容安全 AK/SK 或已禁用，图像审核将降级：带图内容转人工复审");
    }
  }

  @Override
  public ImageReviewResult review(String imageUrl) {
    if (client == null) {
      return ImageReviewResult.uncertain(imageUrl, "图像审核未配置，建议人工确认");
    }
    try {
      String body = "{\"service\":\"baselineCheck\",\"imageUrl\":\"" + imageUrl + "\"}";
      CommonRequest request = new CommonRequest();
      request.setMethod(MethodType.POST);
      request.setDomain("green." + regionId + ".aliyuncs.com");
      request.setVersion("2020-07-13");
      request.setAction("ImageModeration");
      request.setHttpContentType(FormatType.JSON);
      request.setHttpContent(body.getBytes(StandardCharsets.UTF_8), "UTF-8", FormatType.JSON);

      CommonResponse response = client.getCommonResponse(request);
      JsonNode root = objectMapper.readTree(response.getData());
      if (root.path("code").asInt(-1) != 200) {
        return ImageReviewResult.uncertain(imageUrl, "图像审核返回异常：" + root.path("msg").asText("未知错误"));
      }
      String decision = decide(root);
      if (ImageReviewResult.REJECT.equals(decision)) {
        return ImageReviewResult.reject(imageUrl, "图像命中违规内容（" + labels(root) + "）");
      }
      if (ImageReviewResult.UNCERTAIN.equals(decision)) {
        return ImageReviewResult.uncertain(imageUrl, "图像疑似违规，需人工确认");
      }
      return ImageReviewResult.pass(imageUrl, "图像内容正常");
    } catch (Exception e) {
      log.error("阿里云图像审核调用失败：{}", e.getMessage());
      return ImageReviewResult.uncertain(imageUrl, "图像审核服务异常：" + e.getMessage());
    }
  }

  /**
   * 解析响应：任一结果 block -> 拒绝；任一 review -> 不确定；否则通过
   */
  private String decide(JsonNode root) {
    boolean hasReview = false;
    JsonNode data = root.path("data");
    for (JsonNode item : data) {
      if (item.path("code").asInt(-1) != 200) {
        continue;
      }
      // 任务级 riskLevel 兜底
      String taskRisk = item.path("riskLevel").asText("");
      if ("block".equalsIgnoreCase(taskRisk)) {
        return ImageReviewResult.REJECT;
      }
      if ("review".equalsIgnoreCase(taskRisk)) {
        hasReview = true;
      }
      for (JsonNode r : item.path("result")) {
        String suggestion = r.path("suggestion").asText("");
        if ("block".equalsIgnoreCase(suggestion)) {
          return ImageReviewResult.REJECT;
        }
        if ("review".equalsIgnoreCase(suggestion)) {
          hasReview = true;
        }
      }
    }
    return hasReview ? ImageReviewResult.UNCERTAIN : ImageReviewResult.PASS;
  }

  private String labels(JsonNode root) {
    StringBuilder sb = new StringBuilder();
    for (JsonNode item : root.path("data")) {
      for (JsonNode r : item.path("result")) {
        String label = r.path("label").asText("");
        if (!label.isEmpty() && sb.indexOf(label) < 0) {
          sb.append(sb.length() > 0 ? "," : "").append(label);
        }
      }
    }
    return sb.length() > 0 ? sb.toString() : "未知";
  }
}
