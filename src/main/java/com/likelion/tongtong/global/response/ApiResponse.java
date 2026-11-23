package com.likelion.tongtong.global.response;

public record ApiResponse<T>(String code, String message, T data) {

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return new ApiResponse<>(successCode.getCode(), successCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(SuccessCode successCode) {
        return success(successCode, null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMessage());
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return fail(errorCode.getCode(), message);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
