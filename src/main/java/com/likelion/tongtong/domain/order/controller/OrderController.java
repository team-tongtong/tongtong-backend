package com.likelion.tongtong.domain.order.controller;

import com.likelion.tongtong.domain.order.dto.CancelOrderResponse;
import com.likelion.tongtong.domain.order.dto.OrderDetailResponse;
import com.likelion.tongtong.domain.order.dto.OrderListResponse;
import com.likelion.tongtong.domain.order.dto.PickupOrderRequest;
import com.likelion.tongtong.domain.order.dto.PickupOrderResponse;
import com.likelion.tongtong.domain.order.service.OrderService;
import com.likelion.tongtong.domain.room.entity.RoomStatus;
import com.likelion.tongtong.global.response.ApiResponse;
import com.likelion.tongtong.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(summary = "내 주문 목록 조회",
            description = "고정 사용자의 주문 내역을 최근 참여 순으로 반환합니다. "
                    + "status=CANCELED는 방이 취소됐거나 내가 취소한 주문, 그 외 status는 방 상태가 맞고 내가 취소하지 않은 주문을 반환합니다.")
    @GetMapping
    public ApiResponse<OrderListResponse> getMyOrders(
            @RequestParam(name = "status", required = false) RoomStatus status) {
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.getMyOrders(status));
    }

    @Operation(summary = "주문 취소",
            description = "고정 사용자의 주문 중 모집중인 방에 참여 중인 주문을 취소합니다. 방장이 취소해도 방장만 빠지고 모집방은 유지됩니다.")
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<CancelOrderResponse> cancelOrder(@PathVariable Long orderId) {
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.cancelOrder(orderId));
    }

    @Operation(summary = "픽업 코드 입력",
            description = "배달 완료된 주문에 5자리 픽업 코드를 입력해 수령을 완료합니다. 코드가 맞으면 PICKED_UP으로 바뀝니다.")
    @PostMapping("/{orderId}/pickup")
    public ApiResponse<PickupOrderResponse> pickUpOrder(@PathVariable Long orderId,
                                                        @Valid @RequestBody PickupOrderRequest request) {
        return ApiResponse.success(SuccessCode.SUCCESS, orderService.pickUpOrder(orderId, request.code()));
    }
}
