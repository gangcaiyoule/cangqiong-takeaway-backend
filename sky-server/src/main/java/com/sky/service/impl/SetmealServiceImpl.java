package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 新建套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void add(SetmealDTO setmealDTO) {
        //把大部分属性传出来存到Setmeal的对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //先把setmeal(套餐)的信息保存在setmeal数据库中
        setmealMapper.insert(setmeal);
        //获取一下套餐id，便于后面菜品和套餐对齐
        Long id = setmeal.getId();

        //把List<SetmealDish>传出来存起来
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //把setmealDishes(菜品与套餐的关系)保存入setmeal_dish数据库中
        for (SetmealDish dish : setmealDishes) {
            dish.setSetmealId(id);
        }
        //批量插入数据库setmeal_dish
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 根据套餐id查询套餐
     * @param setmealId
     * @return
     */
    @Override
    public SetmealVO getById(Long setmealId) {
        Setmeal setmeal = setmealMapper.getById(setmealId);
        if (setmeal == null) {
            return null;
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        //查询分类名称
        setmealVO.setCategoryName(categoryMapper.getcategoryNameById(setmeal.getCategoryId()));
        //查询套餐下的菜品
        setmealVO.setSetmealDishes(setmealDishMapper.getDishBySetmealId(setmealId));
        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //查询这个套餐状态
        ids.forEach(id -> {
            SetmealVO setmealVO = getById(id);
            if (Objects.equals(setmealVO.getStatus(), StatusConstant.ENABLE)) {
                //起售状态不能删
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        //先删除套餐菜品表的数据
        setmealDishMapper.deleteBatch(ids);
        //再删除套餐数据
        setmealMapper.deleteBatch(ids);
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        // 1️⃣ 修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        Long setmealId = setmealDTO.getId();

        // 2️⃣ 删除原有关联的菜品数据
        setmealDishMapper.deleteBySetmealId(setmealId);
        // 3. 批量插入新关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            // 在插入前，只需统一设置 setmealId
            setmealDishes.forEach(dish -> dish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
}
