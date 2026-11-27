package com.likelion.tongtong.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long orderId,
        Long roomId,
        String storeName,
        String storeImageUrl,
        String spotName,
        Track track,
        RoomStatus status,
        OrderStatus orderStatus,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recruitDeadline,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime orderedAt
) {

    public static OrderSummaryResponse from(Order order) {
        Room room = order.getRoom();
        return new OrderSummaryResponse(
                order.getId(),
                room.getId(),
                room.getStore().getName(),
                room.getStore().getImageUrl(),
                room.getSpot().getName(),
                room.getTrack(),
                room.getStatus(),
                order.getStatus(),
                room.getRecruitDeadline(),
                order.getCreatedAt()
        );
    }
}
