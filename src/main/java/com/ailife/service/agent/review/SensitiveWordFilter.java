package com.ailife.service.agent.review;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 敏感词过滤器：DFA 字典匹配
 */
@Slf4j
@Component
public class SensitiveWordFilter {

  private static final int MIN_MATCH_LEN = 2;

  private final TrieNode root = new TrieNode();
  private volatile int wordCount = 0;

  @PostConstruct
  public void load() {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("sensitive_words.txt")) {
      if (in == null) {
        log.warn("敏感词库 sensitive_words.txt 不存在");
        return;
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line;
      while ((line = reader.readLine()) != null) {
        String word = line.trim();
        if (!word.isEmpty()) {
          addWord(word);
          wordCount++;
        }
      }
      log.info("敏感词库加载完成，共 {} 词", wordCount);
    } catch (Exception e) {
      log.error("敏感词库加载失败", e);
    }
  }

  /**
   * 查找文本中命中的敏感词
   */
  public List<String> findSensitiveWords(String text) {
    List<String> hits = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return hits;
    }
    int length = text.length();
    for (int i = 0; i < length; i++) {
      TrieNode node = root;
      for (int j = i; j < length; j++) {
        node = node.children.get(text.charAt(j));
        if (node == null) {
          break;
        }
        if (node.isEnd && (j - i + 1) >= MIN_MATCH_LEN) {
          String word = text.substring(i, j + 1);
          if (!hits.contains(word)) {
            hits.add(word);
          }
        }
      }
    }
    return hits;
  }

  /**
   * 将敏感词替换为 *
   */
  public String replaceSensitiveWords(String text) {
    if (text == null || text.isBlank()) {
      return text;
    }
    List<String> words = findSensitiveWords(text);
    String result = text;
    for (String word : words) {
      result = result.replace(word, "*".repeat(word.length()));
    }
    return result;
  }

  private void addWord(String word) {
    TrieNode node = root;
    for (char c : word.toCharArray()) {
      node = node.children.computeIfAbsent(c, k -> new TrieNode());
    }
    node.isEnd = true;
  }

  private static class TrieNode {
    final Map<Character, TrieNode> children = new HashMap<>();
    boolean isEnd;
  }
}
