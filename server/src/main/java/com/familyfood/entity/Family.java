package com.familyfood.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("family")
public class Family {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String inviteCode;
    private Long creatorId;
    private String orderDeadline;
    private Long todayChefId;
    private Integer chefRotateDay;
    private LocalDateTime createTime;
}
