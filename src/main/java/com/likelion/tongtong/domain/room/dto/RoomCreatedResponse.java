package com.likelion.tongtong.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "모집방 생성 결과")
public record RoomCreatedResponse(
        @Schema(description = "생성된 모집방 ID", example = "12")
        Long roomId,

        @Schema(description = "방장의 주문 ID", example = "31")
        Long orderId,

        @Schema(description = "생성 직후 모집 상태", example = "RECRUITING")
        RoomStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(description = "서울 기준 모집 마감 시각", example = "2026-11-25T12:10:00")
        LocalDateTime recruitDeadline
) {

    public static RoomCreatedResponse from(Room room, Order order) {
        return new RoomCreatedResponse(room.getId(), order.getId(), room.getStatus(), room.getRecruitDeadline());
    }
}
