package com.baitapnhom.quanlyktx.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class DichVuDto {

        private DichVuDto() {
        }

        public record Request(
                        @NotBlank @Size(max = 10) String maDV,
                        @NotBlank @Size(max = 50) String tenDV,
                        @NotNull @PositiveOrZero BigDecimal donGia) {
        }

        public record Response(String maDV, String tenDV, BigDecimal donGia) {
        }

        // thoiGianSD bỏ trống = bây giờ.
        public record SuDungRequest(
                        @NotBlank String maSV,
                        @NotBlank String maDV,
                        LocalDateTime thoiGianSD,
                        @NotNull @Min(1) Integer soLuong) {
        }

        public record SuDungResponse(String maSV, String hoTen, String maDV, String tenDV, LocalDateTime thoiGianSD,
                        int soLuong, BigDecimal donGia, BigDecimal thanhTien) {
        }
}
