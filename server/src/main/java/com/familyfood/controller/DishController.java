package com.familyfood.controller;

import com.familyfood.common.BizException;
import com.familyfood.common.Result;
import com.familyfood.security.UserContext;
import com.familyfood.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    // ===== 分类 =====
    @PostMapping("/category")
    public Result<?> addCategory(@RequestBody Map<String, Object> body) {
        return Result.ok(dishService.addCategory(
                UserContext.getFamilyId(),
                (String) body.get("name"),
                body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0));
    }

    @GetMapping("/category/list")
    public Result<?> categories() {
        return Result.ok(dishService.getCategories(UserContext.getFamilyId()));
    }

    @PutMapping("/category/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        dishService.updateCategory(id, body.get("name"));
        return Result.ok();
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        dishService.deleteCategory(id);
        return Result.ok();
    }

    // ===== 菜品 =====
    @PostMapping("/dish")
    public Result<?> addDish(@RequestBody Map<String, Object> body) {
        Object name = body.get("name");
        Object price = body.get("price");
        Object categoryId = body.get("categoryId");
        if (name == null || name.toString().isBlank()) throw new BizException("菜品名称不能为空");
        if (categoryId == null) throw new BizException("请选择分类");
        return Result.ok(dishService.addDish(
                UserContext.getFamilyId(),
                Long.valueOf(categoryId.toString()),
                name.toString(),
                (String) body.get("imageUrl"),
                price != null ? new BigDecimal(price.toString()) : BigDecimal.ZERO,
                (String) body.get("description")));
    }

    @GetMapping("/dish/list")
    public Result<?> dishes(@RequestParam(required = false) Long categoryId,
                            @RequestParam(required = false, defaultValue = "0") int all) {
        if (all == 1) {
            return Result.ok(dishService.getDishesAll(UserContext.getFamilyId(), categoryId));
        }
        return Result.ok(dishService.getDishes(UserContext.getFamilyId(), categoryId));
    }

    @DeleteMapping("/dish/{id}")
    public Result<?> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.ok();
    }

    @PutMapping("/dish/{id}")
    public Result<?> updateDish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        dishService.updateDish(id,
                (String) body.get("name"),
                (String) body.get("imageUrl"),
                body.get("price") != null ? new BigDecimal(body.get("price").toString()) : null,
                (String) body.get("description"),
                body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null);
        return Result.ok();
    }

    @PutMapping("/dish/{id}/status")
    public Result<?> toggleDishStatus(@PathVariable Long id) {
        dishService.toggleStatus(id);
        return Result.ok();
    }
}
