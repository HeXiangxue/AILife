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
 * 地理位置工具：基于用户经纬度与商家经纬度计算距离，支持附近商家推荐
 */
@Component
@RequiredArgsConstructor
public class GeoLocationTool {

  private final MerchantService merchantService;

  @Tool(value = "根据用户的经纬度查找附近的商家，按距离从近到远返回商家列表")
  public String findNearbyMerchants(
      @P("用户经度") BigDecimal longitude,
      @P("用户纬度") BigDecimal latitude,
      @P("查找范围（公里），默认 5") Double maxDistance,
      @P("商家分类筛选，可为空") String category,
      @P("返回商家数量，默认 5") Integer limit) {
    double max = maxDistance == null ? 5 : maxDistance;
    MerchantQuery query = new MerchantQuery();
    query.setLongitude(longitude);
    query.setLatitude(latitude);
    query.setCategory((category == null || category.isBlank()) ? null : category.trim());
    query.setLimit(limit == null ? 10 : limit);
    List<Merchant> merchants = merchantService.search(query);
    // 过滤超出范围的结果
    merchants = merchants.stream()
        .filter(m -> m.getDistanceKm() != null && m.getDistanceKm() <= max)
        .toList();
    if (merchants.isEmpty()) {
      return "附近 " + String.format("%.1f", max) + " 公里内没有找到符合条件的商家";
    }
    return merchants.stream()
        .map(m -> "名称:" + m.getName()
            + "|分类:" + m.getCategory()
            + "|评分:" + m.getRating()
            + "|人均:" + m.getAvgPrice() + "元"
            + "|距离:" + String.format("%.1f", m.getDistanceKm()) + "公里"
            + "|地址:" + m.getAddress())
        .collect(Collectors.joining("\n"));
  }
}
