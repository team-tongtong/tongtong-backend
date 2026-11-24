package com.likelion.tongtong.domain.store.repository;

import com.likelion.tongtong.domain.store.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
