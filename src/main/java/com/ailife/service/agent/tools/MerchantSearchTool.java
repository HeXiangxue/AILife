package com.ailife.service.agent.tools;

import com.ailife.dto.MerchantQuery;
import com.ailife.entity.Merchant;
import com.ailife.service.MerchantService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商家搜索工具：供 Agent 按多条件组合查询商家
 */
@Component
@RequiredArgsConstructor
public class MerchantSearchTool {

  private final MerchantService merchantService;

  @Tool(value = "按条件搜索本地商家，支持分类、关键词、最低评分、最高人均价格等多条件组合，返回匹配的商家列表")
  public String searchMerchant(
      @P("商家分类，例如：火锅、烧烤、咖啡、餐厅、便利店，可为空") String category,
      @P("搜索关键词，匹配商家名称或简介，可为空") String keyword,
      @P("最低评分（0-5），可为空") BigDecimal minRating,
      @P("最高人均价格（元），可为空") BigDecimal maxPrice,
      @P("返回商家数量，默认 5") Integer limit) {
    MerchantQuery query = new MerchantQuery();
    query.setCategory(blankToNull(category));
    query.setKeyword(blankToNull(keyword));
    query.setMinRating(minRating);
    query.setMaxPrice(maxPrice);
    query.setLimit(limit == null ? 5 : limit);
    List<Merchant> merchants = merchantService.search(query);
    return formatMerchants(merchants);
  }

  protected String formatMerchants(List<Merchant> merchants) {
    if (merchants == null || merchants.isEmpty()) {
      return "没有找到符合条件的商家";
    }
    return merchants.stream()
        .map(m -> "名称:" + m.getName()
            + "|分类:" + m.getCategory()
            + "|评分:" + m.getRating()
            + "|人均:" + m.getAvgPrice() + "元"
            + "|地址:" + m.getAddress()
            + (m.getDistanceKm() != null ? "|距离:" + round(m.getDistanceKm()) + "公里" : ""))
        .collect(Collectors.joining("\n"));
  }

  protected String blankToNull(String value) {
    return (value == null || value.isBlank()) ? null : value.trim();
  }

  protected String round(double value) {
    return String.format("%.1f", value);
  }
}
