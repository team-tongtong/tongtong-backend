package com.likelion.tongtong.domain.order.service;

import com.likelion.tongtong.domain.order.dto.CancelOrderResponse;
import com.likelion.tongtong.domain.order.dto.OrderDetailResponse;
import com.likelion.tongtong.domain.order.dto.OrderListResponse;
import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.repository.OrderItemRepository;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
import com.likelion.tongtong.domain.user.service.CurrentUserService;
import com.likelion.tongtong.global.exception.BusinessException;
import com.likelion.tongtong.global.response.ErrorCode;
import java.util.List;
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

    public OrderListResponse getMyOrders(RoomStatus roomStatus) {
        return getOrders(currentUserService.getCurrentUser().getId(), roomStatus);
    }

    // 내가 취소한 주문은 방이 모집중이어도 취소 탭에 보여 준다 (프론트와 합의)
    OrderListResponse getOrders(Long userId, RoomStatus roomStatus) {
        List<Order> orders;
        if (roomStatus == null) {
            orders = orderRepository.findAllByUserId(userId);
        } else if (roomStatus == RoomStatus.CANCELED) {
            orders = orderRepository.findCanceledByUserId(userId, RoomStatus.CANCELED, OrderStatus.CANCELED);
        } else {
            orders = orderRepository.findActiveByUserIdAndRoomStatus(userId, roomStatus, OrderStatus.CANCELED);
        }
        return OrderListResponse.from(orders);
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 참여로 모집이 완료되는 순간과 겹치지 않도록 모집방을 잠그고 최신 상태로 판단한다
        // Order.room은 LAZY여야 이 잠금 조회가 모집방을 처음 읽어 최신 상태가 된다 (주문을 join fetch로 읽으면 예전 값이 남는다)
        roomRepository.findByIdForUpdate(order.getRoom().getId());
        if (!order.isCancelable()) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL);
        }

        order.cancel();
        return CancelOrderResponse.from(order);
    }
}
