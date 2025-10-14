package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 通过DishId查询SetmealId
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐菜品数据如setmeal_dish数据库
     * @param setmealDishes
     */
    void insertbuch(List<SetmealDish> setmealDishes);
}
