package com.likelion.tongtong.domain.spot.repository;

import com.likelion.tongtong.domain.spot.entity.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotRepository extends JpaRepository<Spot, Long> {
}
