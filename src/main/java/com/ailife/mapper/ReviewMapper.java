package com.ailife.mapper;

import com.ailife.entity.Review;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReviewMapper {

  @Select("SELECT * FROM review WHERE id = #{id}")
  Review selectById(Long id);

  @Select("SELECT * FROM review WHERE merchant_id = #{merchantId} AND status = 1 ORDER BY create_time DESC")
  List<Review> selectApprovedByMerchant(Long merchantId);

  @Insert("INSERT INTO review (merchant_id, user_id, content, rating, images, status) "
      + "VALUES (#{merchantId}, #{userId}, #{content}, #{rating}, #{images}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Review review);

  @Update("UPDATE review SET status = #{status}, audit_id = #{auditId} WHERE id = #{id}")
  int updateAuditResult(@Param("id") Long id, @Param("status") Integer status, @Param("auditId") Long auditId);
}
