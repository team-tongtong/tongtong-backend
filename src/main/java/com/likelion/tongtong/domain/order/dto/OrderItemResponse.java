package com.likelion.tongtong.domain.order.dto;

import com.likelion.tongtong.domain.order.entity.OrderItem;

public record OrderItemResponse(
        String name,
        Integer quantity,
        boolean shared
) {

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getMenu().getName(),
                orderItem.getQuantity(),
                orderItem.isShared()
        );
    }
}
