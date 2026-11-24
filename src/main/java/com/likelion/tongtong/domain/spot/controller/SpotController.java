package com.likelion.tongtong.domain.spot.controller;

import com.likelion.tongtong.domain.spot.dto.SpotListResponse;
import com.likelion.tongtong.domain.spot.service.SpotService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/spots")
public class SpotController {

    private final SpotService spotService;

    @GetMapping
    public ApiResponse<SpotListResponse> getSpots() {
        return ApiResponse.success(SuccessCode.SUCCESS, spotService.getSpots());
    }
}
