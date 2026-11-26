package com.likelion.tongtong.domain.order.service;

import com.likelion.tongtong.domain.order.dto.CancelOrderResponse;
import com.likelion.tongtong.domain.order.dto.OrderDetailResponse;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
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
    private final RoomRepository roomRepository;

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

    @Transactional
    public CancelOrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 참여로 모집이 완료되는 순간과 겹치지 않도록 모집방을 잠그고 최신 상태로 판단한다
        roomRepository.findByIdForUpdate(order.getRoom().getId());
        if (!order.isCancelable()) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL);
        }

        order.cancel();
        return CancelOrderResponse.from(order);
    }
}
