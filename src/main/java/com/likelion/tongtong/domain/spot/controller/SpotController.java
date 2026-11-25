package com.likelion.tongtong.domain.spot.controller;

import com.likelion.tongtong.domain.spot.dto.SpotListResponse;
import com.likelion.tongtong.domain.spot.service.SpotService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/spots")
@Tag(name = "통통 스팟", description = "통통 스팟 조회 API")
public class SpotController {

    private final SpotService spotService;

    @GetMapping
    @Operation(summary = "통통 스팟 목록 조회", description = "캠퍼스 내 통통 스팟을 스팟 ID 순서로 반환합니다.")
    public ApiResponse<SpotListResponse> getSpots() {
        return ApiResponse.success(SuccessCode.SUCCESS, spotService.getSpots());
    }
}
