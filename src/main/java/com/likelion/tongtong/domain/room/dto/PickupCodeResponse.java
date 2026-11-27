package com.likelion.tongtong.domain.room.dto;

import com.likelion.tongtong.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "참여자별 픽업 코드 (시연용)")
public record PickupCodeResponse(
        @Schema(description = "주문 ID", example = "31")
        Long orderId,

        @Schema(description = "5자리 픽업 코드", example = "04821")
        String code
) {

    public static PickupCodeResponse from(Order order) {
        return new PickupCodeResponse(order.getId(), order.getPickupCode());
    }
}
