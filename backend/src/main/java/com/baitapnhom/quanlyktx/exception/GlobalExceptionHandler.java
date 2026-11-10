package com.baitapnhom.quanlyktx.exception;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleApp(AppException ex) {
        return build(ex.getErrorCode().getStatus(), ex.getErrorCode(), ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_DATA, "Dữ liệu gửi lên không hợp lệ", details);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_DATA,
                "Thiếu tham số hoặc sai định dạng (ngày: yyyy-MM-dd, giờ: yyyy-MM-ddTHH:mm:ss, tháng: yyyy-MM)", null);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(DataAccessException ex) {
        String triggerMessage = triggerMessage(ex);
        if (triggerMessage != null) {
            return build(HttpStatus.BAD_REQUEST, ErrorCode.CONSTRAINT_VIOLATION, triggerMessage, null);
        }
        if (ex instanceof DataIntegrityViolationException) {
            return build(HttpStatus.CONFLICT, ErrorCode.ALREADY_EXISTS,
                    "Dữ liệu bị trùng khóa hoặc đang được bảng khác sử dụng nên không thực hiện được", null);
        }
        log.error("Lỗi cơ sở dữ liệu", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Lỗi cơ sở dữ liệu", null);
    }

    /** Tìm thông báo SIGNAL SQLSTATE '45000' do trigger/procedure trong MySQL ném ra. */
    private static String triggerMessage(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && "45000".equals(sql.getSQLState())) {
                return sql.getMessage();
            }
        }
        return null;
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code, String message,
            List<String> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(code.name(), message, details));
    }
}
