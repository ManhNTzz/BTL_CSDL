package com.baitapnhom.quanlyktx.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Kết quả 4 truy vấn của đề bài và các số liệu thống kê.
public final class BaoCaoDto {

        private BaoCaoDto() {
        }

        // Truy vấn 1
        public record HoaDonThang(String maSV, String cccd, String hoTen, LocalDate ngaySinh, String lop,
                        String queQuan, String thang, BigDecimal tienPhong, BigDecimal tienDichVu,
                        BigDecimal tienVeThang,
                        BigDecimal tienPhatSinhXe, BigDecimal tongTien, String phongTinhTien) {
                public String soPhong() {
                        return phongTinhTien;
                }
        }

        // Truy vấn 2
        public record DichVuSinhVien(String maSV, String cccd, String hoTen, LocalDate ngaySinh, String lop,
                        String queQuan, String tenDV, BigDecimal tongGiaDV) {
        }

        // Truy vấn 3
        public record KhachTham(String maSV, String cccdSV, String hoTenSV, LocalDate ngaySinhSV, String lop,
                        String queQuan, String cccdKhach, String hoTenKhach, LocalDate ngaySinhKhach, long soLanDenChoi,
                        List<String> cacLanDen) {
        }

        // Truy vấn 4:
        public record DoanhThuDichVu(String maDV, String tenDV, BigDecimal donGia, String thang, BigDecimal soLuong,
                        BigDecimal doanhThuDichVu, BigDecimal doanhThuVeThang, BigDecimal doanhThuPhatSinh,
                        BigDecimal tongDoanhThu) {
        }

        // Tổng thu mỗi tháng của KTX (dùng vẽ biểu đồ).
        public record DoanhThuThang(String thang, BigDecimal tienPhong, BigDecimal tienDichVu, BigDecimal tienVeThang,
                        BigDecimal tienPhatSinhXe, BigDecimal tongThu) {
        }

        public record TongQuan(int soSinhVien, int soPhong, int tongSoCho, int soNguoiDangO, int soChoTrong,
                        int soXeVeThang, int soVeThangThangNay, int soLuotKhachThangNay, BigDecimal doanhThuThangNay) {
        }
}
