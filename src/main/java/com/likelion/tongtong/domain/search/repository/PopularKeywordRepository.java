package com.likelion.tongtong.domain.search.repository;

import com.likelion.tongtong.domain.search.entity.PopularKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {

    List<PopularKeyword> findTop10ByOrderByRankingAscIdAsc();
}
