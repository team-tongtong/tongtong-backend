package com.likelion.tongtong.domain.store.repository;

import com.likelion.tongtong.domain.store.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findTop2ByStoreIdOrderByIdAsc(Long storeId);
}
