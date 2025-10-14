package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐管理接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result add(@RequestBody SetmealDTO setmealDTO) {
        log.info("开始新增菜品: {}", setmealDTO);
        setmealService.add(setmealDTO);
        return Result.success();
    }
}
