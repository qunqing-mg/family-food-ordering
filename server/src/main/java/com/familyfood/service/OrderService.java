package com.familyfood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.common.BizException;
import com.familyfood.dto.OrderRequest;
import com.familyfood.entity.*;
import com.familyfood.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper itemMapper;
    private final DishMapper dishMapper;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;
    private final StatsService statsService;
    private final NotificationService notiService;

    @Transactional
    public Order createOrder(Long userId, Long familyId, OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException("购物车为空");
        }

        // 下单截止检查
        Family family = familyMapper.selectById(familyId);
        if (family != null && family.getOrderDeadline() != null && !family.getOrderDeadline().isBlank()) {
            String now = java.time.LocalTime.now().toString().substring(0, 5);
            if (now.compareTo(family.getOrderDeadline()) > 0) {
                throw new BizException("已过下单截止时间 " + family.getOrderDeadline() + "，明天再来吧");
            }
        }

        User user = userMapper.selectById(userId);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (var it : request.getItems()) {
            Dish dish = dishMapper.selectById(it.getDishId());
            if (dish == null || dish.getStatus() == 0) {
                throw new BizException("菜品" + it.getDishId() + "已下架或不存在");
            }
            int qty = it.getQuantity() != null ? it.getQuantity() : 1;

            OrderItem item = new OrderItem();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishImage(dish.getImageUrl());
            item.setQuantity(qty);
            item.setPrice(dish.getPrice());
            items.add(item);

            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        Order order = new Order();
        order.setFamilyId(familyId);
        order.setUserId(userId);
        order.setUserNickname(user != null ? user.getNickname() : null);
        order.setRemark(request.getRemark());
        order.setTotalAmount(total);
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            itemMapper.insert(item);
            // 记录常点菜品
            statsService.recordOrder(userId, item.getDishId());
        }

        // 发通知给家人
        String chef = user != null && user.getNickname() != null ? user.getNickname() : "有人";
        notiService.notifyFamily(familyId, userId, "new_order",
                chef + "刚下了单，快去接单吧！");

        return order;
    }

    public List<Map<String, Object>> getOrders(Long familyId, Long userId, String type) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getFamilyId, familyId);

        if ("my".equals(type)) {
            wrapper.eq(Order::getUserId, userId);
        }

        List<Order> orders = orderMapper.selectList(wrapper.orderByDesc(Order::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Order o : orders) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("userId", o.getUserId());
            m.put("userNickname", o.getUserNickname());
            m.put("totalAmount", o.getTotalAmount());
            m.put("remark", o.getRemark());
            m.put("status", o.getStatus());
            m.put("createTime", o.getCreateTime());

            List<OrderItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            m.put("items", items);

            result.add(m);
        }
        return result;
    }

    public void updateStatus(Long orderId, int status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
}
