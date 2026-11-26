package com.likelion.tongtong.domain.room.dto;

import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "모집방 참여 요청")
public record JoinRoomRequest(
        @NotNull(message = "담은 메뉴 목록을 입력해 주세요.")
        @Schema(description = "아껴통은 담은 메뉴(1개 이상), 나눠통은 개별 주문 메뉴(0개 이상). 함께 나눌 메뉴는 자동으로 포함돼요.")
        List<@NotNull(message = "메뉴 항목을 입력해 주세요.") @Valid MenuItem> items,

        @NotNull(message = "결제 수단을 선택해 주세요.")
        PaymentMethod paymentMethod
) {

    public record MenuItem(
            @NotNull(message = "메뉴 ID를 입력해 주세요.")
            @Positive(message = "메뉴 ID를 1 이상으로 입력해 주세요.")
            Long menuId,

            @NotNull(message = "메뉴 수량을 입력해 주세요.")
            @Positive(message = "메뉴 수량을 1개 이상으로 입력해 주세요.")
            Integer quantity
    ) {
    }
}
