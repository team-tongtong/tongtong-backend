package com.likelion.tongtong.domain.order.service;

import com.likelion.tongtong.domain.order.dto.OrderDetailResponse;
import com.likelion.tongtong.domain.order.dto.OrderListResponse;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.user.service.CurrentUserService;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CurrentUserService currentUserService;

    public OrderDetailResponse getOrder(Long orderId) {
        Order order = orderRepository.findDetailById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 취소한 주문은 모집 현황에서 빠진다
        long currentParticipants = orderRepository.countByRoomIdAndStatusNot(
                order.getRoom().getId(), OrderStatus.CANCELED);

        return OrderDetailResponse.from(
                order,
                currentParticipants,
                orderItemRepository.findAllWithMenuByOrderId(orderId)
        );
    }

    public OrderListResponse getMyOrders(RoomStatus roomStatus) {
        return getOrders(currentUserService.getCurrentUser().getId(), roomStatus);
    }

    OrderListResponse getOrders(Long userId, RoomStatus roomStatus) {
        return OrderListResponse.from(orderRepository.findAllByUserIdAndRoomStatus(userId, roomStatus));
    }
}
