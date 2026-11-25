package com.likelion.tongtong.domain.search.dto;

import com.likelion.tongtong.domain.search.entity.PopularKeyword;
import java.util.List;

public record PopularKeywordListResponse(List<KeywordResponse> keywords) {

    public static PopularKeywordListResponse from(List<PopularKeyword> popularKeywords) {
        return new PopularKeywordListResponse(popularKeywords.stream()
                .map(KeywordResponse::from)
                .toList());
    }

    public record KeywordResponse(Integer rank, String keyword) {

        private static KeywordResponse from(PopularKeyword popularKeyword) {
            return new KeywordResponse(popularKeyword.getRanking(), popularKeyword.getKeyword());
        }
    }
}
