package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long userId;
    private String userNickname;
    private BigDecimal totalAmount;
    private String remark;
    private Integer status; // 0待接单 1已接单 2已完成 3已取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
