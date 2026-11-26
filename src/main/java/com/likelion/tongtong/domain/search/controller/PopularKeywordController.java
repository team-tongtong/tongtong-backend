package com.likelion.tongtong.domain.search.controller;

import com.likelion.tongtong.domain.search.dto.PopularKeywordListResponse;
import com.likelion.tongtong.domain.search.service.PopularKeywordService;
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
@RequestMapping("/api/v1/search/popular-keywords")
@Tag(name = "검색", description = "검색 관련 조회 API")
public class PopularKeywordController {

    private final PopularKeywordService popularKeywordService;

    @GetMapping
    @Operation(summary = "인기 검색어 조회", description = "저장된 인기 검색어를 순위 오름차순으로 최대 10개 반환합니다.")
    public ApiResponse<PopularKeywordListResponse> getPopularKeywords() {
        return ApiResponse.success(SuccessCode.SUCCESS, popularKeywordService.getPopularKeywords());
    }
}
