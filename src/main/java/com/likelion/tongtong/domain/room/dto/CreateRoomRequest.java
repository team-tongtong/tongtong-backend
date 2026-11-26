package com.likelion.tongtong.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.likelion.tongtong.domain.order.entity.PaymentMethod;
import com.likelion.tongtong.domain.room.entity.Track;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "모집방 생성 요청")
public record CreateRoomRequest(
        @NotNull(message = "가게 ID를 입력해 주세요.")
        @Positive(message = "가게 ID를 1 이상으로 입력해 주세요.")
        Long storeId,

        @NotNull(message = "주문 방식을 선택해 주세요.")
        Track track,

        @Valid
        @Schema(description = "나눠통은 함께 나눌 메뉴를 지정하고, 아껴통은 null을 사용합니다.",
                types = {"object", "null"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        SharedMenu sharedMenu,

        @NotNull(message = "개인 메뉴 목록을 입력해 주세요.")
        @Schema(description = "개인 메뉴 목록. 아껴통은 1개 이상이며 나눠통은 빈 목록을 허용합니다.")
        List<@NotNull(message = "개인 메뉴 항목을 입력해 주세요.") @Valid MenuItem> items,

        @NotNull(message = "통통 스팟 ID를 입력해 주세요.")
        @Positive(message = "통통 스팟 ID를 1 이상으로 입력해 주세요.")
        Long spotId,

        @NotNull(message = "모집 인원을 입력해 주세요.")
        @Min(value = 2, message = "모집 인원은 2명 이상 6명 이하로 정해 주세요.")
        @Max(value = 6, message = "모집 인원은 2명 이상 6명 이하로 정해 주세요.")
        Integer maxParticipants,

        @NotNull(message = "모집 시간을 입력해 주세요.")
        @Min(value = 10, message = "모집 시간은 10분 이상 60분 이하로 정해 주세요.")
        @Max(value = 60, message = "모집 시간은 10분 이상 60분 이하로 정해 주세요.")
        @Schema(description = "10분 단위의 모집 시간", example = "10")
        Integer recruitMinutes,

        @NotNull(message = "결제 수단을 선택해 주세요.")
        PaymentMethod paymentMethod
) {

    @AssertTrue(message = "아껴통은 개인 메뉴를 1개 이상, 나눠통은 함께 나눌 메뉴를 지정해 주세요.")
    @JsonIgnore
    public boolean isMenuSelectionValid() {
        if (track == null || items == null) {
            return true;
        }
        return switch (track) {
            case INDIVIDUAL -> sharedMenu == null && !items.isEmpty();
            case PORTION -> sharedMenu != null;
        };
    }

    @AssertTrue(message = "모집 시간을 10분 단위로 정해 주세요.")
    @JsonIgnore
    public boolean isRecruitMinutesStepValid() {
        return recruitMinutes == null || recruitMinutes % 10 == 0;
    }

    public record SharedMenu(
            @NotNull(message = "함께 나눌 메뉴 ID를 입력해 주세요.")
            @Positive(message = "함께 나눌 메뉴 ID를 1 이상으로 입력해 주세요.")
            Long menuId
    ) {
    }

    public record MenuItem(
            @NotNull(message = "개인 메뉴 ID를 입력해 주세요.")
            @Positive(message = "개인 메뉴 ID를 1 이상으로 입력해 주세요.")
            Long menuId,

            @NotNull(message = "메뉴 수량을 입력해 주세요.")
            @Positive(message = "메뉴 수량을 1개 이상으로 입력해 주세요.")
            Integer quantity
    ) {
    }
}
