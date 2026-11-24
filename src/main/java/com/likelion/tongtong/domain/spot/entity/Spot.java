package com.likelion.tongtong.domain.spot.entity;

import com.likelion.tongtong.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "spot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spot_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(length = 500)
    private String mapImageUrl;

    @Builder
    private Spot(String name, String description, String mapImageUrl) {
        this.name = name;
        this.description = description;
        this.mapImageUrl = mapImageUrl;
    }
}
