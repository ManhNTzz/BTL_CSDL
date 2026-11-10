package com.baitapnhom.quanlyktx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class XeDto {

    private XeDto() {
    }

    public record XeRequest(
            @NotBlank @Size(max = 15) String bienSo,
            @NotBlank @Size(max = 30) String loaiXe,
            @NotBlank String maSV, String thang) {
    }

    public record XeResponse(String bienSo, String loaiXe, String maSV, String hoTenChuXe) {
    }

    public record VeThangRequest(
            @NotBlank String bienSo,
            @NotBlank String thang,
            @PositiveOrZero BigDecimal donGiaVe) {
    }

    public record VeThangResponse(String bienSo, String thang, BigDecimal donGiaVe) {
    }

    public record LuotRequest(
            @NotBlank String bienSo,
            LocalDateTime thoiGianGui,
            LocalDateTime thoiGianLay) {
    }

    public record CapLayRequest(
            @NotBlank String bienSo,
            @NotNull LocalDateTime thoiGianGui,
            @NotNull LocalDateTime thoiGianLay) {
    }

    public record LuotResponse(String bienSo, LocalDateTime thoiGianGui, LocalDateTime thoiGianLay,
            BigDecimal soTienPhaiTra) {
    }

    public record GiaHanResponse(String thang, int soVeDaTao, int soVeDaCoSan) {
    }
}
