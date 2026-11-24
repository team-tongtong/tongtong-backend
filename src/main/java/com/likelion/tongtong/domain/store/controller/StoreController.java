package com.likelion.tongtong.domain.store.controller;

import com.likelion.tongtong.domain.store.dto.StoreListResponse;
import com.likelion.tongtong.domain.store.service.StoreService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가게", description = "가게 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 전체 목록 조회", description = "가게 전체를 한 번에 반환합니다. 검색어 필터와 정렬은 프론트에서 처리합니다.")
    @GetMapping
    public ApiResponse<StoreListResponse> getStores() {
        return ApiResponse.success(SuccessCode.SUCCESS, storeService.getStores());
    }
}
