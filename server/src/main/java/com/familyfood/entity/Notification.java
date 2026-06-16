package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long familyId;
    private Long userId;
    private String type;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}
