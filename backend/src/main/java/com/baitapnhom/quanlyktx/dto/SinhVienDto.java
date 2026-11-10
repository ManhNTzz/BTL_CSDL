package com.baitapnhom.quanlyktx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public final class SinhVienDto {

    private SinhVienDto() {
    }

    public record Request(
            @NotBlank @Pattern(regexp = "\\d{12}", message = "CCCD phải gồm đúng 12 chữ số") String cccd,
            @NotBlank @Size(max = 10) String maSV,
            @NotBlank @Size(max = 100) String hoTen,
            @NotNull LocalDate ngaySinh,
            @NotBlank @Size(max = 20) String lop,
            @NotBlank @Size(max = 100) String queQuan) {
    }

    public record UpdateRequest(
            @NotBlank @Size(max = 100) String hoTen,
            @NotNull LocalDate ngaySinh,
            @NotBlank @Size(max = 20) String lop,
            @NotBlank @Size(max = 100) String queQuan) {
    }

    public record Response(String maSV, String cccd, String hoTen, LocalDate ngaySinh, String lop, String queQuan) {
    }
    // Hồ sơ đầy đủ của một sinh viên: phòng, xe, dịch vụ, khách đến thăm và hóa đơn các tháng.
    public record HoSo(Response thongTin, PhongDto.OPhongResponse phongHienTai,
            List<PhongDto.OPhongResponse> lichSuPhong, List<XeDto.XeResponse> xe,
            List<XeDto.VeThangResponse> veThang, List<DichVuDto.SuDungResponse> dichVuDaDung,
            List<KhachDto.LuotThamResponse> khachDenTham, List<BaoCaoDto.HoaDonThang> hoaDon) {
    }
}
