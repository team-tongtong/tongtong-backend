package com.likelion.tongtong.domain.store.controller;

import com.likelion.tongtong.domain.store.dto.StoreListResponse;
import com.likelion.tongtong.domain.store.service.StoreService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ApiResponse<StoreListResponse> getStores() {
        return ApiResponse.success(SuccessCode.SUCCESS, storeService.getStores());
    }
}
