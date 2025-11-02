package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户信息
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenId(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    /**
     * 通过userId获取用户信息
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 统计截止某天的总用户量
     * @param endTime
     * @return
     */
    @Select("select count(*) from user where create_time < #{endTime}")
    Integer countTotalUserUntil(LocalDateTime endTime);

    /**
     * 统计新增用户数量
     * @param beginTime
     * @param endTime
     * @return
     */
    @Select("select count(*) from user where create_time > #{beginTime} and create_time < #{endTime}")
    Integer countNewUserByDate(LocalDateTime beginTime, LocalDateTime endTime);
}
