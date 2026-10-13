package com.ailife.service.agent.tools;

import com.ailife.dto.MerchantQuery;
import com.ailife.entity.Merchant;
import com.ailife.entity.Review;
import com.ailife.mapper.ReviewMapper;
import com.ailife.service.MerchantService;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 评价查询工具：供 Agent 查询商家口碑
 */
@Component
@RequiredArgsConstructor
public class ReviewSearchTool {

  private final MerchantService merchantService;
  private final ReviewMapper reviewMapper;

  @Tool(value = "查询某个商家的用户评价与口碑信息，返回评价数量、平均评分、好评率以及评价详情")
  public String queryMerchantReviews(
      @P("商家名称") String merchantName,
      @P("返回几条评价详情，默认 3") Integer limit) {
    List<Merchant> merchants = merchantService.search(byName(merchantName));
    if (merchants.isEmpty()) {
      return "没有找到名为「" + merchantName + "」的商家";
    }
    Merchant merchant = merchants.get(0);
    List<Review> reviews = reviewMapper.selectApprovedByMerchant(merchant.getId());

    StringBuilder sb = new StringBuilder();
    sb.append("商家：").append(merchant.getName()).append("\n");
    if (reviews.isEmpty()) {
      sb.append("该商家暂无用户评价");
      return sb.toString();
    }
    // 统计评价数量、平均评分、好评率（评分>=4 视为好评）
    int count = reviews.size();
    double avgRating = reviews.stream()
        .mapToInt(Review::getRating)
        .average().orElse(0);
    long positive = reviews.stream().filter(r -> r.getRating() != null && r.getRating() >= 4).count();
    BigDecimal positiveRate = BigDecimal.valueOf(positive * 100.0 / count).setScale(1, RoundingMode.HALF_UP);

    sb.append("评价数量：").append(count).append(" 条\n");
    sb.append("平均评分：").append(String.format("%.1f", avgRating)).append(" 分\n");
    sb.append("好评率：").append(positiveRate).append("%\n");
    sb.append("评价详情：\n");
    int max = limit == null ? 3 : Math.min(limit, 5);
    reviews.stream().limit(max).forEach(r ->
        sb.append("- ").append(r.getRating()).append(" 星：").append(r.getContent()).append("\n"));
    return sb.toString();
  }

  private MerchantQuery byName(String name) {
    MerchantQuery query = new MerchantQuery();
    query.setKeyword(name);
    query.setLimit(1);
    return query;
  }
}
