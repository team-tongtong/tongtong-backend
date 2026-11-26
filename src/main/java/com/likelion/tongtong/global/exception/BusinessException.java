package com.likelion.tongtong.global.exception;

import com.likelion.tongtong.global.response.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 같은 코드라도 상황에 맞는 문구를 화면에 보여 줘야 할 때 쓴다 (예: INVALID_INPUT)
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
