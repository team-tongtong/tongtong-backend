package com.likelion.tongtong.domain.store.dto;

import com.likelion.tongtong.domain.store.entity.Store;
import java.math.BigDecimal;

public record StoreSummaryResponse(
        Long storeId,
        String name,
        String category,
        String imageUrl,
        BigDecimal rating,
        Integer reviewCount,
        Integer orderCount,
        Integer minOrderAmount,
        Integer deliveryFee,
        Integer deliveryMinutes,
        Integer distanceMeters
) {

    public static StoreSummaryResponse from(Store store) {
        return new StoreSummaryResponse(
                store.getId(),
                store.getName(),
                store.getCategory(),
                store.getImageUrl(),
                store.getRating(),
                store.getReviewCount(),
                store.getOrderCount(),
                store.getMinOrderAmount(),
                store.getDeliveryFee(),
                store.getDeliveryMinutes(),
                store.getDistanceMeters()
        );
    }
}
