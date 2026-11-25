package com.likelion.tongtong.domain.spot.dto;

import com.likelion.tongtong.domain.spot.entity.Spot;

public record SpotResponse(Long spotId, String name, String description, String mapImageUrl) {

    public static SpotResponse from(Spot spot) {
        return new SpotResponse(spot.getId(), spot.getName(), spot.getDescription(), spot.getMapImageUrl());
    }
}
