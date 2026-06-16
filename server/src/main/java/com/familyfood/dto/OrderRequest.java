package com.familyfood.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String remark;
    private List<OrderItemDto> items;

    @Data
    public static class OrderItemDto {
        private Long dishId;
        private Integer quantity;
    }
}
