package com.baitapnhom.quanlyktx.exception;

import java.util.List;

public record ErrorResponse(String code, String message, List<String> details) {
}
