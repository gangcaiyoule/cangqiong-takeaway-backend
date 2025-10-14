package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 添加菜品
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 查询页码
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 通过菜品ID获取菜品信息
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 一步删除多个菜品
     * @param ids
     */
//    @Delete("delete from dish where id = #{id}")
    void deleteByDishIds(List<Long> ids);

    /**
     * 一步删除多个菜品关联的口味
     * @param dish_ids
     */
    void deleteFlavorByDishIds(List<Long> dish_ids);

    /**
     * 修改菜品信息(除了口味)
     * @param dish
     */
    void update(Dish dish);

    /**
     * 删除菜品关联的口味
     * @param dish_ids
     */
    @Delete("delete from dish_flavor where dish_id = #{dish_id}")
    void deleteFlavorByDishId(Long dish_ids);

    List<Dish> list(Dish dish);

    /**
     * 根据菜品id修改菜品状态
     * @param dish
     */
    @Update("update dish set status = #{status} where id = #{id}")
    void updateStatus(Dish dish);
}
