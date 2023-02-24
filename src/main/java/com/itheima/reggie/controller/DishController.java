package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param age
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        //分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件过滤器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> DishDtoRecords = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(DishDtoRecords);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息进行回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        dishService.deleteWithFlavors(ids);
        return R.success("删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status,Long ids){
        log.info("要修改成的状态为:{},菜品ID为:{}",status,ids);
        dishService.updateStatus(status,ids);
        return R.success("修改状态成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        queryWrapper.eq(Dish::getStatus,1);
        List<Dish> dishList = dishService.list(queryWrapper);
        List<DishDto> collect = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, id);
            List<DishFlavor> list = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(list);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(collect);
    }

}
