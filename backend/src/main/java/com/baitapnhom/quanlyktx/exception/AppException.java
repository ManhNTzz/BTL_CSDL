package com.baitapnhom.quanlyktx.exception;

// Bắt lỗi GlobalExceptionHandler
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.NOT_FOUND, message);
    }

    public static AppException exists(String message) {
        return new AppException(ErrorCode.ALREADY_EXISTS, message);
    }

    public static AppException invalid(String message) {
        return new AppException(ErrorCode.INVALID_DATA, message);
    }

    // Vi phạm ràng buộc của đề bài (số người trong phòng, tối đa 2 xe, ...).
    public static AppException rule(String message) {
        return new AppException(ErrorCode.CONSTRAINT_VIOLATION, message);
    }
}
