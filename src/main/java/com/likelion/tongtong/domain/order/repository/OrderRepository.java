package com.likelion.tongtong.domain.order.repository;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
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

    List<Order> findAllByRoomIdAndStatusNot(Long roomId, OrderStatus status);

    interface RoomParticipantCount {
        Long getRoomId();

        long getParticipantCount();
    }
}
