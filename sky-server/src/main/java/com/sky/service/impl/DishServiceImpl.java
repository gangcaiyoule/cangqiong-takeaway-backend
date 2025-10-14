package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 新增菜品及其口味
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override

    public void saveWithFlavor(DishDTO dishDTO) {
        //加入一条菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //顺便获取一下id值，方便下面"插入口味"对齐菜品的id
        Long dishid = dish.getId();
        //插入口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishid);
            });
            dishFlavorMapper.batchInsert(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //该菜品是否在起售状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //该菜品是否有套餐包含
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
//        //删除该菜品
//        for (Long id : ids) {
//            dishMapper.deleteByDishId(id);
//        }
        //删除该菜品关联的口味
//        for (Long id : ids) {
//            dishMapper.deleteFlavorByDishId(id);
//        }
        //代码性能优化
        //一句sql删除菜品(delete from dish where id in dish_ids)
        //删除该菜品
        dishMapper.deleteByDishIds(ids);
        //删除该菜品关联的口味
        dishMapper.deleteFlavorByDishIds(ids);
    }

    /**
     * 根据ID查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //查询dish表
        Dish dish = dishMapper.getById(id);
        //查询dish_flavor
        List<DishFlavor> dishFlavor = dishFlavorMapper.getDishFlavorById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavor);
        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    @AutoFill(value = OperationType.UPDATE)
    public void update(DishDTO dishDTO) {
        //修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //单独修改菜品口味(先删除，再上传)
        //删除
        dishMapper.deleteFlavorByDishId(dish.getId());
        //上传
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());
            });
            dishFlavorMapper.batchInsert(flavors);
        }
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getDishFlavorById(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        //封装成一个Dish对象
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.updateStatus(dish);
    }
}
