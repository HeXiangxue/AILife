package com.ailife.mapper;

import com.ailife.entity.SysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserMapper {

  @Select("SELECT * FROM sys_user WHERE username = #{username}")
  SysUser selectByUsername(String username);

  @Select("SELECT * FROM sys_user WHERE id = #{id}")
  SysUser selectById(Long id);

  @Insert("INSERT INTO sys_user (username, password, nickname, phone, avatar, reputation_score, role, status) "
      + "VALUES (#{username}, #{password}, #{nickname}, #{phone}, #{avatar}, #{reputationScore}, #{role}, #{status})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(SysUser user);

  @Update("UPDATE sys_user SET reputation_score = #{score} WHERE id = #{id}")
  int updateReputation(@Param("id") Long id, @Param("score") Integer score);
}
