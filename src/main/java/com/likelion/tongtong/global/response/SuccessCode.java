package com.likelion.tongtong.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    SUCCESS("SUCCESS", "요청에 성공했습니다.", HttpStatus.OK),
    CREATED("CREATED", "생성되었습니다.", HttpStatus.CREATED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
