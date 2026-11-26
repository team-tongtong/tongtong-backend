package com.likelion.tongtong.domain.search.dto;

import com.likelion.tongtong.domain.search.entity.PopularKeyword;
import java.util.List;

public record PopularKeywordListResponse(List<PopularKeywordResponse> keywords) {

    public static PopularKeywordListResponse from(List<PopularKeyword> popularKeywords) {
        return new PopularKeywordListResponse(popularKeywords.stream()
                .map(PopularKeywordResponse::from)
                .toList());
    }
}
