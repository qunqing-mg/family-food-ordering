package com.familyfood.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {}
