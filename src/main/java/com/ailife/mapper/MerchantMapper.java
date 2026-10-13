package com.ailife.mapper;

import com.ailife.dto.MerchantQuery;
import com.ailife.entity.Merchant;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantMapper {

  /**
   * 多条件组合搜索，支持按距离排序
   */
  List<Merchant> search(MerchantQuery query);

  @Select("SELECT * FROM merchant WHERE id = #{id}")
  Merchant selectById(Long id);

  @Select("SELECT DISTINCT category FROM merchant WHERE status = 1")
  List<String> selectCategories();

  @Select("SELECT id, name FROM merchant WHERE status = 1")
  List<Merchant> selectIdAndName();

  @Insert("INSERT INTO merchant (name, category, address, longitude, latitude, phone, rating, avg_price, open_hours, description, status) "
      + "VALUES (#{name}, #{category}, #{address}, #{longitude}, #{latitude}, #{phone}, #{rating}, #{avgPrice}, #{openHours}, #{description}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Merchant merchant);
}
