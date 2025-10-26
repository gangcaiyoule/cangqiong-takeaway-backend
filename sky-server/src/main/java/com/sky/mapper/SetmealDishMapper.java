package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
    void insertbatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询套餐下的菜品
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getDishBySetmealId(Long setmealId);

    /**
     * 批量删除套餐菜品关联表
     * @param setmeal_ids
     */
    void deleteBatch(List<Long> setmeal_ids);
}
