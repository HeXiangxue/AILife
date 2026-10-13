package com.ailife.service;

import com.ailife.common.BizException;
import com.ailife.dto.MerchantQuery;
import com.ailife.entity.Merchant;
import com.ailife.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantService {

  private final MerchantMapper merchantMapper;

  public List<Merchant> search(MerchantQuery query) {
    if (query.getLimit() == null) {
      query.setLimit(20);
    }
    return merchantMapper.search(query);
  }

  public Merchant getById(Long id) {
    Merchant merchant = merchantMapper.selectById(id);
    if (merchant == null) {
      throw new BizException(404, "商家不存在");
    }
    return merchant;
  }

  public Merchant create(Merchant merchant) {
    if (merchant.getStatus() == null) {
      merchant.setStatus(1);
    }
    merchantMapper.insert(merchant);
    return merchant;
  }

  public List<String> categories() {
    return merchantMapper.selectCategories();
  }
}
