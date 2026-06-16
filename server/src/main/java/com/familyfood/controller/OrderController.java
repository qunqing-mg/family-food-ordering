package com.familyfood.controller;

import com.familyfood.common.Result;
import com.familyfood.dto.OrderRequest;
import com.familyfood.security.UserContext;
import com.familyfood.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.familyfood.mapper.OrderItemMapper;
import com.familyfood.entity.OrderItem;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemMapper itemMapper;

    @PostMapping
    public Result<?> create(@RequestBody OrderRequest request) {
        return Result.ok(orderService.createOrder(
                UserContext.getUserId(), UserContext.getFamilyId(), request));
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "all") String type) {
        return Result.ok(orderService.getOrders(
                UserContext.getFamilyId(), UserContext.getUserId(), type));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        orderService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @PutMapping("/item/{itemId}/feedback")
    public Result<?> feedback(@PathVariable Long itemId, @RequestBody Map<String, Integer> body) {
        OrderItem item = itemMapper.selectById(itemId);
        if (item != null) {
            item.setFeedback(body.get("feedback"));
            itemMapper.updateById(item);
        }
        return Result.ok();
    }
}
