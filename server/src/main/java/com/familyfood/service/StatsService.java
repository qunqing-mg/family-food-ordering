package com.familyfood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.entity.*;
import com.familyfood.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final DishMapper dishMapper;
    private final FrequentDishMapper freqMapper;
    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final UserMapper userMapper;

    public Map<String, Object> getDashboard(Long familyId, Long userId) {
        Map<String, Object> data = new LinkedHashMap<>();

        // 家庭总消费
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getFamilyId, familyId)
                        .eq(Order::getStatus, 2));
        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalSpent", totalSpent);
        data.put("totalOrders", orders.size());

        // 菜品排行榜（top 10）
        List<Order> allOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getFamilyId, familyId)
                        .in(Order::getStatus, 1, 2));
        Map<String, Integer> dishRank = new LinkedHashMap<>();
        for (Order o : allOrders) {
            List<OrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            for (OrderItem it : items) {
                dishRank.merge(it.getDishName(), it.getQuantity(), Integer::sum);
            }
        }
        List<Map<String, Object>> topDishes = new ArrayList<>();
        dishRank.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey()); m.put("count", e.getValue());
                    topDishes.add(m);
                });
        data.put("topDishes", topDishes);

        // 今日主厨
        Family family = familyMapper.selectById(familyId);
        String chefName = null;
        if (family != null && family.getTodayChefId() != null) {
            User chef = userMapper.selectById(family.getTodayChefId());
            chefName = chef != null ? chef.getNickname() : null;
        }
        data.put("todayChef", chefName);

        // 常点菜品（当前用户）
        List<FrequentDish> freqList = freqMapper.selectList(
                new LambdaQueryWrapper<FrequentDish>().eq(FrequentDish::getUserId, userId)
                        .orderByDesc(FrequentDish::getOrderCount).last("LIMIT 6"));
        List<Map<String, Object>> freqDishes = new ArrayList<>();
        for (FrequentDish fd : freqList) {
            Dish dish = dishMapper.selectById(fd.getDishId());
            if (dish != null && dish.getStatus() == 1) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("dishId", dish.getId()); m.put("name", dish.getName());
                m.put("price", dish.getPrice()); m.put("imageUrl", dish.getImageUrl());
                m.put("orderCount", fd.getOrderCount());
                freqDishes.add(m);
            }
        }
        data.put("freqDishes", freqDishes);

        // 猜你喜欢：家庭热门但当前用户还没点过的菜
        Set<Long> userDishIds = new HashSet<>();
        for (FrequentDish fd : freqMapper.selectList(
                new LambdaQueryWrapper<FrequentDish>().eq(FrequentDish::getUserId, userId))) {
            userDishIds.add(fd.getDishId());
        }
        List<Map<String, Object>> recommend = new ArrayList<>();
        for (Map.Entry<String, Integer> e : dishRank.entrySet()) {
            if (recommend.size() >= 4) break;
            String dishName = e.getKey();
            // 找到菜名对应的菜品（上架中）
            Dish activeDish = dishMapper.selectOne(new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getFamilyId, familyId).eq(Dish::getName, dishName).eq(Dish::getStatus, 1));
            if (activeDish != null && !userDishIds.contains(activeDish.getId())) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("dishId", activeDish.getId()); m.put("name", activeDish.getName());
                m.put("imageUrl", activeDish.getImageUrl()); m.put("price", activeDish.getPrice());
                recommend.add(m);
            }
        }
        data.put("recommend", recommend);

        return data;
    }

    public void recordOrder(Long userId, Long dishId) {
        FrequentDish fd = freqMapper.selectOne(new LambdaQueryWrapper<FrequentDish>()
                .eq(FrequentDish::getUserId, userId).eq(FrequentDish::getDishId, dishId));
        if (fd != null) {
            fd.setOrderCount(fd.getOrderCount() + 1);
            fd.setLastOrderTime(java.time.LocalDateTime.now());
            freqMapper.updateById(fd);
        } else {
            fd = new FrequentDish();
            fd.setUserId(userId); fd.setDishId(dishId);
            fd.setOrderCount(1);
            freqMapper.insert(fd);
        }
    }

    public void rotateChef(Long familyId) {
        List<FamilyMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<FamilyMember>().eq(FamilyMember::getFamilyId, familyId));
        if (members.isEmpty()) return;
        Family family = familyMapper.selectById(familyId);
        int day = family.getChefRotateDay() != null ? family.getChefRotateDay() + 1 : 0;
        int idx = day % members.size();
        family.setTodayChefId(members.get(idx).getUserId());
        family.setChefRotateDay(day);
        familyMapper.updateById(family);
    }
}
