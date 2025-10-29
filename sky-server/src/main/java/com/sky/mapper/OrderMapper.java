package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);
}
