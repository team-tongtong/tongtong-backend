package com.likelion.tongtong.domain.store.repository;

import com.likelion.tongtong.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
