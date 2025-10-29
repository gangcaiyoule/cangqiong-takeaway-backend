package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订餐详细数据
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);
}
