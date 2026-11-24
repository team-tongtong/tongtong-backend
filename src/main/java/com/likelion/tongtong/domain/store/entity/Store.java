package com.likelion.tongtong.domain.store.entity;

import com.likelion.tongtong.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(nullable = false)
    private Integer reviewCount;

    @Column(nullable = false)
    private Integer orderCount;

    @Column(nullable = false)
    private Integer minOrderAmount;

    @Column(nullable = false)
    private Integer deliveryFee;

    @Column(nullable = false)
    private Integer deliveryMinutes;

    @Column(nullable = false)
    private Integer distanceMeters;

    @Column(nullable = false)
    private LocalTime lastOrderTime;

    @Builder
    private Store(String name, String category, String imageUrl, BigDecimal rating, Integer reviewCount,
                  Integer orderCount, Integer minOrderAmount, Integer deliveryFee, Integer deliveryMinutes,
                  Integer distanceMeters, LocalTime lastOrderTime) {
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.orderCount = orderCount;
        this.minOrderAmount = minOrderAmount;
        this.deliveryFee = deliveryFee;
        this.deliveryMinutes = deliveryMinutes;
        this.distanceMeters = distanceMeters;
        this.lastOrderTime = lastOrderTime;
    }
}
