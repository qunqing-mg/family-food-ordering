package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("frequent_dish")
public class FrequentDish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long dishId;
    private Integer orderCount;
    private LocalDateTime lastOrderTime;
}
