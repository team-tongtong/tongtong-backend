package com.likelion.tongtong.domain.order.controller;

import com.likelion.tongtong.domain.order.dto.CancelOrderResponse;
import com.likelion.tongtong.domain.order.dto.OrderDetailResponse;
import com.likelion.tongtong.domain.order.service.OrderService;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "주문", description = "주문 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 상세 조회", description = "주문 정보와 모집 현황, 담은 메뉴, 취소 가능 여부를 반환합니다.")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable Long orderId) {
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.getOrder(orderId));
    }

    @Operation(summary = "주문 취소",
            description = "모집중인 방에 참여 중인 주문을 취소합니다. 방장이 취소해도 방장만 빠지고 모집방은 유지됩니다.")
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<CancelOrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.cancelOrder(orderId));
    }
}
