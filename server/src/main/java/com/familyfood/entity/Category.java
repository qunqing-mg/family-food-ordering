package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
