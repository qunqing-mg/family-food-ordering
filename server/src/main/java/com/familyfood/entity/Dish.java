package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long categoryId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String description;
    private Integer status; // 1=上架 0=下架
    private LocalDateTime createTime;
}
