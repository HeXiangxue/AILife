package com.ailife.service.agent.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图像审核结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageReviewResult {

  public static final String PASS = "PASS";
  public static final String REJECT = "REJECT";
  public static final String UNCERTAIN = "UNCERTAIN";

  private String url;
  private String decision;
  private String labels;
  private String message;

  public static ImageReviewResult pass(String url, String message) {
    return new ImageReviewResult(url, PASS, null, message);
  }

  public static ImageReviewResult reject(String url, String message) {
    return new ImageReviewResult(url, REJECT, null, message);
  }

  public static ImageReviewResult uncertain(String url, String message) {
    return new ImageReviewResult(url, UNCERTAIN, null, message);
  }
}
