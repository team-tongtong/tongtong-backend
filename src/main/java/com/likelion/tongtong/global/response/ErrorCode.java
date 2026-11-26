package com.likelion.tongtong.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", "요청 본문 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("INVALID_INPUT", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER("INVALID_PARAMETER", "요청 파라미터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", "지원하지 않는 미디어 타입입니다.", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 가게·메뉴·스팟
    STORE_NOT_FOUND("STORE_NOT_FOUND", "가게를 찾을 수 없어요.", HttpStatus.NOT_FOUND),
    SPOT_NOT_FOUND("SPOT_NOT_FOUND", "스팟을 찾을 수 없어요.", HttpStatus.NOT_FOUND),
    MENU_NOT_FOUND("MENU_NOT_FOUND", "메뉴를 찾을 수 없어요.", HttpStatus.NOT_FOUND),
    MENU_STORE_MISMATCH("MENU_STORE_MISMATCH", "다른 가게의 메뉴가 섞여 있어요.", HttpStatus.BAD_REQUEST),
    NOT_PORTIONABLE_MENU("NOT_PORTIONABLE_MENU", "이 메뉴는 나눠 담을 수 없어요.", HttpStatus.BAD_REQUEST),

    // 모집방
    ROOM_NOT_FOUND("ROOM_NOT_FOUND", "모집방을 찾을 수 없어요.", HttpStatus.NOT_FOUND),
    RECRUIT_CLOSED("RECRUIT_CLOSED", "모집이 마감되었어요.", HttpStatus.CONFLICT),
    ALREADY_JOINED("ALREADY_JOINED", "이미 참여한 모집방이에요.", HttpStatus.CONFLICT),
    INVALID_STATUS_TRANSITION("INVALID_STATUS_TRANSITION", "모집 완료 상태가 아니에요.", HttpStatus.CONFLICT),

    // 주문·픽업
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "주문 내역을 찾을 수 없어요.", HttpStatus.NOT_FOUND),
    CANNOT_CANCEL("CANNOT_CANCEL", "지금은 취소할 수 없어요.", HttpStatus.CONFLICT),
    INVALID_PICKUP_CODE("INVALID_PICKUP_CODE", "픽업 코드가 맞지 않아요.", HttpStatus.BAD_REQUEST),
    NOT_ARRIVED("NOT_ARRIVED", "아직 배달이 완료되지 않았어요.", HttpStatus.CONFLICT),
    ALREADY_PICKED_UP("ALREADY_PICKED_UP", "이미 수령한 주문이에요.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
