package com.likelion.tongtong.domain.order.dto;

import com.likelion.tongtong.domain.order.entity.Order;
import java.util.List;

public record OrderListResponse(
        List<OrderSummaryResponse> orders
) {

    public static OrderListResponse from(List<Order> orders) {
        return new OrderListResponse(
                orders.stream()
                        .map(OrderSummaryResponse::from)
                        .toList()
        );
    }
}
