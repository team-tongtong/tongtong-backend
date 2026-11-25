package com.likelion.tongtong.domain.spot.dto;

import com.likelion.tongtong.domain.spot.entity.Spot;
import java.util.List;

public record SpotListResponse(List<SpotResponse> spots) {

    public static SpotListResponse from(List<Spot> spots) {
        return new SpotListResponse(spots.stream().map(SpotResponse::from).toList());
    }
}
