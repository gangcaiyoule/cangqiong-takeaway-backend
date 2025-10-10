package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 添加菜品
     * @param flavors
     */

    void batchInsert(List<DishFlavor> flavors);

    /**
     * 查询菜品口味
     * @param dish_id
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dish_id}")
    List<DishFlavor> getDishFlavorById(Long dish_id);
}