package com.likelion.tongtong.domain.search.repository;

import com.likelion.tongtong.domain.search.entity.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {
}
