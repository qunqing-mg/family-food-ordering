package com.familyfood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BizException;
import com.familyfood.entity.Category;
import com.familyfood.entity.Dish;
import com.familyfood.mapper.CategoryMapper;
import com.familyfood.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    // ===== 分类 =====
    public Category addCategory(Long familyId, String name, Integer sortOrder) {
        Category c = new Category();
        c.setFamilyId(familyId);
        c.setName(name);
        c.setSortOrder(sortOrder != null ? sortOrder : 0);
        categoryMapper.insert(c);
        return c;
    }

    public List<Category> getCategories(Long familyId) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getFamilyId, familyId)
                .orderByAsc(Category::getSortOrder));
    }

    public void updateCategory(Long id, String name) {
        Category c = categoryMapper.selectById(id);
        if (c == null) throw new BizException("分类不存在");
        c.setName(name);
        categoryMapper.updateById(c);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    // ===== 菜品 =====
    public Dish addDish(Long familyId, Long categoryId, String name, String imageUrl,
                        java.math.BigDecimal price, String description) {
        Dish d = new Dish();
        d.setFamilyId(familyId);
        d.setCategoryId(categoryId);
        d.setName(name);
        d.setImageUrl(imageUrl);
        d.setPrice(price);
        d.setDescription(description);
        d.setStatus(1);
        dishMapper.insert(d);
        return d;
    }

    public List<Dish> getDishes(Long familyId, Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getFamilyId, familyId)
                .eq(Dish::getStatus, 1);
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        return dishMapper.selectList(wrapper.orderByDesc(Dish::getCreateTime));
    }

    public List<Dish> getDishesAll(Long familyId, Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getFamilyId, familyId);
        if (categoryId != null && categoryId > 0) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        return dishMapper.selectList(wrapper.orderByDesc(Dish::getCreateTime));
    }

    public void updateDish(Long id, String name, String imageUrl,
                           java.math.BigDecimal price, String description, Long categoryId) {
        Dish d = dishMapper.selectById(id);
        if (d == null) throw new BizException("菜品不存在");
        if (name != null) d.setName(name);
        if (imageUrl != null) d.setImageUrl(imageUrl);
        if (price != null) d.setPrice(price);
        if (description != null) d.setDescription(description);
        if (categoryId != null) d.setCategoryId(categoryId);
        dishMapper.updateById(d);
    }

    public void toggleStatus(Long id) {
        Dish d = dishMapper.selectById(id);
        if (d == null) throw new BizException("菜品不存在");
        d.setStatus(d.getStatus() == 1 ? 0 : 1);
        dishMapper.updateById(d);
    }

    public void deleteDish(Long id) {
        Dish d = dishMapper.selectById(id);
        if (d == null) throw new BizException("菜品不存在");
        dishMapper.deleteById(id);
    }
}
