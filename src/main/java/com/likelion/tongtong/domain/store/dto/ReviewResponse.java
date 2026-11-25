package com.likelion.tongtong.domain.store.dto;

import com.likelion.tongtong.domain.store.entity.Review;

public record ReviewResponse(
        String nickname,
        Integer rating,
        String content
) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getNickname(),
                review.getRating(),
                review.getContent()
        );
    }
}
