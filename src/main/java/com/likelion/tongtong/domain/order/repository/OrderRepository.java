package com.likelion.tongtong.domain.order.repository;

import com.likelion.tongtong.domain.order.entity.Order;
import com.likelion.tongtong.domain.order.entity.OrderStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            select o from Order o
            join fetch o.room r
            join fetch r.store
            join fetch r.spot
            where o.id = :orderId
            """)
    Optional<Order> findDetailById(@Param("orderId") Long orderId);

    long countByRoomIdAndStatusNot(Long roomId, OrderStatus status);
}
