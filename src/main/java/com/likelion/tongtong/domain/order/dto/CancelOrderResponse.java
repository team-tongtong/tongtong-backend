package com.likelion.tongtong.domain.order.dto;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;

public record CancelOrderResponse(
        Long orderId,
        OrderStatus orderStatus
) {

    public static CancelOrderResponse from(Order order) {
        return new CancelOrderResponse(order.getId(), order.getStatus());
    }
}
