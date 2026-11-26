package com.likelion.tongtong.domain.search.service;

import com.likelion.tongtong.domain.search.dto.PopularKeywordListResponse;
import com.likelion.tongtong.domain.search.repository.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularKeywordService {

    private final PopularKeywordRepository popularKeywordRepository;

    public PopularKeywordListResponse getPopularKeywords() {
        return PopularKeywordListResponse.from(popularKeywordRepository.findTop10ByOrderByRankingAscIdAsc());
    }
}
