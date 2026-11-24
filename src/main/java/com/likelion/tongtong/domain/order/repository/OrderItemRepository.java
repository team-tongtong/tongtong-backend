package com.likelion.tongtong.domain.order.repository;

import com.likelion.tongtong.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
