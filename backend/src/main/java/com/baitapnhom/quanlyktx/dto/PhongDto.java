package com.baitapnhom.quanlyktx.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class PhongDto {

    private PhongDto() {
    }

    public record PhongRequest(
            @NotBlank @Size(max = 10) String soPhong,
            @NotBlank @Size(max = 30) String loaiPhong,
            @NotNull @PositiveOrZero BigDecimal donGia,
            @NotNull @Min(1) Integer soNguoiToiDa) {
    }

    public record PhongResponse(String soPhong, String loaiPhong, BigDecimal donGia, int soNguoiToiDa,
            int soNguoiDangO, int conCho) {
    }

    // ngayKetThuc bỏ trống = đang ở.
    public record OPhongRequest(
            @NotBlank String maSV,
            @NotBlank String soPhong,
            @NotNull LocalDate ngayBatDau,
            LocalDate ngayKetThuc) {
    }

    public record KetThucRequest(
            @NotBlank String maSV,
            @NotBlank String soPhong,
            @NotNull LocalDate ngayBatDau,
            @NotNull LocalDate ngayKetThuc) {
    }

    public record ChuyenPhongRequest(
            @NotBlank String maSV,
            @NotBlank String soPhongMoi,
            @NotNull LocalDate ngayChuyen) {
    }

    public record OPhongResponse(String maSV, String hoTen, String soPhong, String loaiPhong, BigDecimal donGia,
            LocalDate ngayBatDau, LocalDate ngayKetThuc) {
    }
}
