package com.likelion.tongtong.domain.order.repository;

import com.likelion.tongtong.domain.order.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select oi from OrderItem oi
            join fetch oi.menu
            where oi.order.id = :orderId
            order by oi.id asc
            """)
    List<OrderItem> findAllWithMenuByOrderId(@Param("orderId") Long orderId);
}
