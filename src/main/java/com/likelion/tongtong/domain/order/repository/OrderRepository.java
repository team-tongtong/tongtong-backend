package com.likelion.tongtong.domain.order.repository;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select orders.room.id as roomId, count(orders) as participantCount
            from Order orders
            where orders.room.id in :roomIds and orders.status = :status
            group by orders.room.id
            """)
    List<RoomParticipantCount> countByRoomIdsAndStatus(@Param("roomIds") Collection<Long> roomIds,
                                                      @Param("status") OrderStatus status);

    @Query("""
            select o from Order o
            join fetch o.room r
            join fetch r.store
            join fetch r.spot
            where o.id = :orderId
            """)
    Optional<Order> findDetailById(@Param("orderId") Long orderId);

    long countByRoomIdAndStatusNot(Long roomId, OrderStatus status);

    // 주문 내역 전체
    @Query("""
            select o from Order o
            join fetch o.room r
            join fetch r.store
            join fetch r.spot
            where o.user.id = :userId
            order by o.createdAt desc, o.id desc
            """)
    List<Order> findAllByUserId(@Param("userId") Long userId);

    // 모집중·모집 완료·배달 완료 탭: 방 상태가 맞고 내가 취소하지 않은 주문
    @Query("""
            select o from Order o
            join fetch o.room r
            join fetch r.store
            join fetch r.spot
            where o.user.id = :userId
              and r.status = :roomStatus
              and o.status <> :canceledOrderStatus
            order by o.createdAt desc, o.id desc
            """)
    List<Order> findActiveByUserIdAndRoomStatus(@Param("userId") Long userId,
                                                @Param("roomStatus") RoomStatus roomStatus,
                                                @Param("canceledOrderStatus") OrderStatus canceledOrderStatus);

    // 취소 탭: 방이 취소됐거나 내가 취소한 주문
    @Query("""
            select o from Order o
            join fetch o.room r
            join fetch r.store
            join fetch r.spot
            where o.user.id = :userId
              and (r.status = :canceledRoomStatus or o.status = :canceledOrderStatus)
            order by o.createdAt desc, o.id desc
            """)
    List<Order> findCanceledByUserId(@Param("userId") Long userId,
                                     @Param("canceledRoomStatus") RoomStatus canceledRoomStatus,
                                     @Param("canceledOrderStatus") OrderStatus canceledOrderStatus);

    interface RoomParticipantCount {
        Long getRoomId();

        long getParticipantCount();
    }
}
