package com.likelion.tongtong.domain.room.dto;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.PaymentStatus;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모집방 참여 결과")
public record JoinRoomResponse(
        @Schema(description = "내 주문 ID", example = "42")
        Long orderId,

        @Schema(description = "참여 후 모집방 상태. 이 참여로 다 찼으면 RECRUIT_COMPLETED", example = "RECRUIT_COMPLETED")
        RoomStatus roomStatus,

        @Schema(description = "내 음식값(원)", example = "9450")
        Integer foodAmount,

        @Schema(description = "배달비 분담(원)", example = "500")
        Integer deliveryFeeShare,

        @Schema(description = "소분 수수료(원)", example = "500")
        Integer portionFee,

        @Schema(description = "내 결제 금액(원)", example = "10450")
        Integer totalAmount,

        @Schema(description = "PENDING(결제 대기). 이 참여로 모집 완료되면 PAID", example = "PAID")
        PaymentStatus paymentStatus
) {

    public static JoinRoomResponse from(Room room, Order order) {
        return new JoinRoomResponse(
                order.getId(),
                room.getStatus(),
                order.getFoodAmount(),
                order.getDeliveryFeeShare(),
                order.getPortionFee(),
                order.getTotalAmount(),
                order.getPaymentStatus()
        );
    }
}
