package com.likelion.tongtong.domain.room.service;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.order.repository.OrderRepository;
import com.likelion.tongtong.domain.room.entity.Room;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.domain.room.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomAutoCancelService {

    private final RoomRepository roomRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Long> findExpiredRoomIds(LocalDateTime now) {
        return roomRepository.findExpiredRoomIds(RoomStatus.RECRUITING, now);
    }

    // 방마다 트랜잭션을 따로 열어, 한 방에서 실패해도 다른 방은 계속 처리한다
    @Transactional
    public boolean cancelIfExpired(Long roomId, LocalDateTime now) {
        // 조회와 잠금 사이에 참여로 모집이 완료됐을 수 있어 잠근 뒤 다시 확인한다
        Room room = roomRepository.findByIdForUpdate(roomId).orElse(null);
        if (room == null || !room.isExpired(now)) {
            return false;
        }

        room.cancelRecruit();
        // 모집 완료 전이라 결제된 금액이 없어 주문과 결제 상태만 취소로 바꾼다
        List<Order> activeOrders = orderRepository.findAllByRoomIdAndStatusNot(roomId, OrderStatus.CANCELED);
        activeOrders.forEach(Order::cancel);
        return true;
    }
}
