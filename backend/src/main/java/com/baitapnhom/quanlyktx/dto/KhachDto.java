package com.baitapnhom.quanlyktx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class KhachDto {

        private KhachDto() {
        }

        public record Response(String cccd, String hoTen, LocalDate ngaySinh) {
        }

        // Khách mới cần thêm hoTen và ngaySinh; khách đã có chỉ cần cccdKhach.
        // ngayDenChoi bỏ trống = bây giờ.
        public record LuotThamRequest(
                        @NotBlank @Pattern(regexp = "\\d{12}", message = "CCCD phải gồm đúng 12 chữ số") String cccdKhach,
                        @Size(max = 100) String hoTen,
                        LocalDate ngaySinh,
                        @NotBlank String maSV,
                        LocalDateTime ngayDenChoi) {
        }

        public record LuotThamResponse(String cccdKhach, String hoTenKhach, String maSV, String hoTenSV,
                        LocalDateTime ngayDenChoi) {
        }
}
