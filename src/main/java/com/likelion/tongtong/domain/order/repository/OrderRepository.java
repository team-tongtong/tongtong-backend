package com.likelion.tongtong.domain.order.repository;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import java.util.Collection;
import java.util.List;
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

    interface RoomParticipantCount {
        Long getRoomId();

        long getParticipantCount();
    }
}
