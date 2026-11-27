package com.likelion.tongtong.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "픽업 코드 입력 요청")
public record PickupOrderRequest(
        @NotNull(message = "픽업 코드를 입력해 주세요.")
        @Pattern(regexp = "\\d{5}", message = "픽업 코드는 숫자 5자리로 입력해 주세요.")
        @Schema(description = "5자리 숫자 문자열", example = "04821")
        String code
) {
}
