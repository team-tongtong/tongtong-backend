package com.likelion.tongtong.domain.room.dto;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "배달 완료 전환 결과")
public record ArriveRoomResponse(
        @Schema(description = "모집방 ID", example = "12")
        Long roomId,

        @Schema(description = "항상 ARRIVED", example = "ARRIVED")
        RoomStatus status,

        @Schema(description = "참여자별 픽업 코드. 시연용")
        List<PickupCodeResponse> pickupCodes
) {

    public static ArriveRoomResponse from(Room room, List<Order> orders) {
        return new ArriveRoomResponse(
                room.getId(),
                room.getStatus(),
                orders.stream().map(PickupCodeResponse::from).toList()
        );
    }
}
