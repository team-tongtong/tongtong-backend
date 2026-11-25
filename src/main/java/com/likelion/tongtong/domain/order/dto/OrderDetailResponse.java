package com.likelion.tongtong.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderItem;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.entity.Track;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        Long roomId,
        String storeName,
        String spotName,
        Track track,
        RoomStatus status,
        OrderStatus orderStatus,
        long currentParticipants,
        Integer maxParticipants,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recruitDeadline,
        List<OrderItemResponse> items,
        Integer totalAmount,
        PaymentMethod paymentMethod,
        boolean cancelable
) {

    public static OrderDetailResponse from(Order order, long currentParticipants, List<OrderItem> items) {
        Room room = order.getRoom();
        return new OrderDetailResponse(
                order.getId(),
                room.getId(),
                room.getStore().getName(),
                room.getSpot().getName(),
                room.getTrack(),
                room.getStatus(),
                order.getStatus(),
                currentParticipants,
                room.getMaxParticipants(),
                room.getRecruitDeadline(),
                items.stream().map(OrderItemResponse::from).toList(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.isCancelable()
        );
    }
}
