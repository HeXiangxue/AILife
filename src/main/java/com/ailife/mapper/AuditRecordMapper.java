package com.ailife.mapper;

import com.ailife.entity.AuditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AuditRecordMapper {

  @Insert("INSERT INTO audit_record (target_type, target_id, text_sensitive_words, image_result, reputation_result, "
      + "llm_result, risk_level, audit_status) "
      + "VALUES (#{targetType}, #{targetId}, #{textSensitiveWords}, #{imageResult}, #{reputationResult}, "
      + "#{llmResult}, #{riskLevel}, #{auditStatus})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(AuditRecord record);

  @Select("SELECT * FROM audit_record WHERE id = #{id}")
  AuditRecord selectById(Long id);

  @Select("SELECT * FROM audit_record WHERE audit_status = #{status} ORDER BY create_time DESC LIMIT 200")
  List<AuditRecord> selectByStatus(String status);

  @Select("SELECT * FROM audit_record ORDER BY create_time DESC LIMIT 200")
  List<AuditRecord> selectAll();

  @Update("UPDATE audit_record SET audit_status = #{auditStatus}, review_comment = #{reviewComment}, "
      + "reviewer_id = #{reviewerId}, review_time = NOW() WHERE id = #{id}")
  int updateReview(@Param("id") Long id, @Param("auditStatus") String auditStatus,
      @Param("reviewComment") String reviewComment, @Param("reviewerId") Long reviewerId);
}
