package com.likelion.tongtong.domain.search.dto;

import com.likelion.tongtong.domain.search.entity.PopularKeyword;

public record PopularKeywordResponse(Integer rank, String keyword) {

    public static PopularKeywordResponse from(PopularKeyword popularKeyword) {
        return new PopularKeywordResponse(popularKeyword.getRanking(), popularKeyword.getKeyword());
    }
}
