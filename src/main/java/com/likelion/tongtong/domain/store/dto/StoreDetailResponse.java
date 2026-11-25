package com.likelion.tongtong.domain.store.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.likelion.tongtong.domain.store.entity.Menu;
import com.likelion.tongtong.domain.store.entity.Review;
import com.likelion.tongtong.domain.store.entity.Store;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record StoreDetailResponse(
        Long storeId,
        String name,
        String imageUrl,
        BigDecimal rating,
        Integer reviewCount,
        @JsonFormat(pattern = "HH:mm")
        LocalTime lastOrderTime,
        Integer minOrderAmount,
        Integer deliveryFee,
        Integer deliveryMinutes,
        List<MenuResponse> menus,
        List<ReviewResponse> reviews
) {

    public static StoreDetailResponse from(Store store, List<Menu> menus, List<Review> reviews) {
        return new StoreDetailResponse(
                store.getId(),
                store.getName(),
                store.getImageUrl(),
                store.getRating(),
                store.getReviewCount(),
                store.getLastOrderTime(),
                store.getMinOrderAmount(),
                store.getDeliveryFee(),
                store.getDeliveryMinutes(),
                menus.stream().map(MenuResponse::from).toList(),
                reviews.stream().map(ReviewResponse::from).toList()
        );
    }
}
