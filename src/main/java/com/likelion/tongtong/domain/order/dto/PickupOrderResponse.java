package com.likelion.tongtong.domain.order.dto;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;

public record PickupOrderResponse(
        Long orderId,
        OrderStatus orderStatus
) {

    public static PickupOrderResponse from(Order order) {
        return new PickupOrderResponse(order.getId(), order.getStatus());
    }
}
