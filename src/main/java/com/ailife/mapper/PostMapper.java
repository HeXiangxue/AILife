package com.ailife.mapper;

import com.ailife.entity.Post;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper {

  @Select("SELECT * FROM post WHERE id = #{id}")
  Post selectById(Long id);

  @Select("SELECT * FROM post WHERE status = #{status} ORDER BY create_time DESC LIMIT 100")
  List<Post> selectByStatus(Integer status);

  @Select("SELECT * FROM post WHERE user_id = #{userId} ORDER BY create_time DESC")
  List<Post> selectByUser(Long userId);

  @Insert("INSERT INTO post (user_id, title, content, images, status) "
      + "VALUES (#{userId}, #{title}, #{content}, #{images}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Post post);

  @Update("UPDATE post SET status = #{status}, audit_id = #{auditId} WHERE id = #{id}")
  int updateAuditResult(@Param("id") Long id, @Param("status") Integer status, @Param("auditId") Long auditId);
}
