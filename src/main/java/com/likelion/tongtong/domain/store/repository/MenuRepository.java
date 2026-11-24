package com.likelion.tongtong.domain.store.repository;

import com.likelion.tongtong.domain.store.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {
}
