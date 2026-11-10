package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.dto.BaoCaoDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BaoCaoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BaoCaoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Truy vấn 1
    public List<BaoCaoDto.HoaDonThang> hoaDon(LocalDate thang, String maSV) {
        if (thang != null) {
            String pMaSV = (maSV != null && !maSV.isBlank()) ? maSV.trim() : null;
            return jdbc.getJdbcTemplate().query("CALL sp_hoa_don_thang(?, ?)", (rs, i) -> {
                LocalDate ngayThang = rs.getObject("Thang", LocalDate.class);
                String thangStr = ngayThang != null ? ngayThang.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        : thang.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                return new BaoCaoDto.HoaDonThang(
                        rs.getString("MaSV"),
                        rs.getString("CCCD"),
                        rs.getString("HoTen"),
                        rs.getObject("NgaySinh", LocalDate.class),
                        rs.getString("Lop"),
                        rs.getString("QueQuan"),
                        thangStr,
                        rs.getBigDecimal("TienPhong"),
                        rs.getBigDecimal("TienDichVu"),
                        rs.getBigDecimal("TienVeThang"),
                        rs.getBigDecimal("PhuPhiGuiXe"),
                        rs.getBigDecimal("TongPhaiTra"), rs.getString("PhongTinhTien"));
            }, thang, pMaSV);
        }

        List<LocalDate> cacThang = cacThangBaoCao();
        List<BaoCaoDto.HoaDonThang> ketQua = new ArrayList<>();
        for (LocalDate m : cacThang) {
            ketQua.addAll(hoaDon(m, maSV));
        }
        return ketQua;
    }

    // Truy vấn 2
    public List<BaoCaoDto.DichVuSinhVien> dichVuSinhVien(LocalDate tuNgay, LocalDate denNgay) {
        return jdbc.getJdbcTemplate().query("CALL sp_dich_vu_trong_khoang(?, ?)", (rs, i) -> new BaoCaoDto.DichVuSinhVien(
                rs.getString("MaSV"),
                rs.getString("CCCD"),
                rs.getString("HoTen"),
                rs.getObject("NgaySinh", LocalDate.class),
                rs.getString("Lop"),
                rs.getString("QueQuan"),
                rs.getString("TenDV"),
                rs.getBigDecimal("TongTien")),
                tuNgay, denNgay);
    }

    // Truy vấn 3
    public List<BaoCaoDto.KhachTham> khachTham(LocalDate tuNgay, LocalDate denNgay) {
        String sql = """
                SELECT s.MaSV,ns.CCCD AS cccd_sv,ns.HoTen AS ho_ten_sv,ns.NgaySinh AS ngay_sinh_sv,
                       s.Lop,s.QueQuan,nk.CCCD AS cccd_khach,nk.HoTen AS ho_ten_khach,
                       nk.NgaySinh AS ngay_sinh_khach,l.NgayDenChoi
                FROM LUOT_THAM l JOIN SINH_VIEN s ON s.MaSV=l.MaSV
                JOIN NGUOI ns ON ns.CCCD=s.CCCD JOIN KHACH k ON k.CCCD=l.CCCD_Khach
                JOIN NGUOI nk ON nk.CCCD=k.CCCD
                WHERE l.NgayDenChoi>=? AND l.NgayDenChoi<DATE_ADD(?,INTERVAL 1 DAY)
                ORDER BY s.MaSV,nk.CCCD,l.NgayDenChoi
                """;
        List<BaoCaoDto.KhachTham> events = jdbc.getJdbcTemplate().query(sql, (rs,i) -> new BaoCaoDto.KhachTham(
                rs.getString("MaSV"),rs.getString("cccd_sv"),rs.getString("ho_ten_sv"),
                rs.getObject("ngay_sinh_sv",LocalDate.class),rs.getString("Lop"),rs.getString("QueQuan"),
                rs.getString("cccd_khach"),rs.getString("ho_ten_khach"),rs.getObject("ngay_sinh_khach",LocalDate.class),
                1,List.of(rs.getObject("NgayDenChoi",java.time.LocalDateTime.class).toString())),tuNgay,denNgay);
        java.util.Map<String,List<BaoCaoDto.KhachTham>> groups = new java.util.LinkedHashMap<>();
        for (BaoCaoDto.KhachTham event:events) groups.computeIfAbsent(event.maSV()+"|"+event.cccdKhach(), k->new ArrayList<>()).add(event);
        return groups.values().stream().map(group->{
            BaoCaoDto.KhachTham first=group.get(0);
            List<String> times=group.stream().flatMap(e->e.cacLanDen().stream()).toList();
            return new BaoCaoDto.KhachTham(first.maSV(),first.cccdSV(),first.hoTenSV(),first.ngaySinhSV(),first.lop(),first.queQuan(),
                    first.cccdKhach(),first.hoTenKhach(),first.ngaySinhKhach(),times.size(),times);
        }).sorted(java.util.Comparator.comparing(BaoCaoDto.KhachTham::maSV)
            .thenComparing(java.util.Comparator.comparingLong(BaoCaoDto.KhachTham::soLanDenChoi).reversed())
            .thenComparing(BaoCaoDto.KhachTham::cccdKhach)).toList();
    }

    // Truy vấn 4
    private static final String SQL_DOANH_THU_THANG = """
            WITH dich_vu_thuong AS (
                SELECT l.MaDV, SUM(l.SoLuong * d.DonGia) AS SoTien, SUM(l.SoLuong) AS SoLuong
                FROM LAN_SU_DUNG l
                JOIN DICH_VU d ON d.MaDV = l.MaDV AND d.TenDV <> 'Trông xe'
                WHERE l.ThoiGianSD >= ? AND l.ThoiGianSD < DATE_ADD(?, INTERVAL 1 MONTH)
                GROUP BY l.MaDV
            ),
            ve AS (
                SELECT COALESCE(SUM(DonGiaVe), 0) AS SoTien, COUNT(*) AS SoLuong
                FROM VE_THANG
                WHERE ThangDangKy = ?
            ),
            gui AS (
                SELECT COALESCE(SUM(SoTienPhaiTra), 0) AS SoTien, COUNT(*) AS SoLuong
                FROM V_LUOT_GUI_LAY_XE
                WHERE ThoiGianGui >= ? AND ThoiGianGui < DATE_ADD(?, INTERVAL 1 MONTH)
            )
            SELECT d.MaDV, d.TenDV, d.DonGia AS DonGiaNiemYet,
                   CASE WHEN d.TenDV = 'Trông xe' THEN NULL
                                 ELSE COALESCE(dt.SoLuong, 0) END AS SoLuong,
                   COALESCE(dt.SoTien, 0) AS DoanhThuDichVu,
                   IF(d.TenDV = 'Trông xe', ve.SoTien, 0) AS DoanhThuVeThang,
                   IF(d.TenDV = 'Trông xe', gui.SoTien, 0) AS DoanhThuPhatSinh,
                   CASE WHEN d.TenDV = 'Trông xe' THEN ve.SoTien + gui.SoTien
                        ELSE COALESCE(dt.SoTien, 0) END AS TongDoanhThu
            FROM DICH_VU d
            LEFT JOIN dich_vu_thuong dt ON dt.MaDV = d.MaDV
            CROSS JOIN ve
            CROSS JOIN gui
            ORDER BY TongDoanhThu DESC, d.MaDV
            """;

    // thang (ngày 01 của tháng) có thể null: null = mọi tháng.
    public List<BaoCaoDto.DoanhThuDichVu> doanhThuDichVu(LocalDate thang) {
        if (thang != null) {
            if (jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM VE_THANG WHERE ThangDangKy=?", Long.class, thang) > 0
                    && jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM DICH_VU WHERE TenDV='Trông xe'", Long.class) == 0) {
                throw com.baitapnhom.quanlyktx.exception.AppException.rule("Thiếu danh mục Trông xe; không thể tổng hợp tiền xe");
            }
            String thangStr = thang.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            return jdbc.getJdbcTemplate().query(SQL_DOANH_THU_THANG, (rs, i) -> new BaoCaoDto.DoanhThuDichVu(
                    rs.getString("MaDV"),
                    rs.getString("TenDV"),
                    rs.getBigDecimal("DonGiaNiemYet"),
                    thangStr,
                    rs.getBigDecimal("SoLuong"),
                    rs.getBigDecimal("DoanhThuDichVu"),
                    rs.getBigDecimal("DoanhThuVeThang"),
                    rs.getBigDecimal("DoanhThuPhatSinh"),
                    rs.getBigDecimal("TongDoanhThu")),
                    thang, thang, thang, thang, thang);
        }

        List<LocalDate> cacThang = cacThangBaoCao();
        List<BaoCaoDto.DoanhThuDichVu> ketQua = new ArrayList<>();
        for (LocalDate m : cacThang) {
            ketQua.addAll(doanhThuDichVu(m));
        }
        return ketQua;
    }

    // Mọi tháng có thể phát sinh tiền, kể cả tháng nằm giữa hai đầu đợt ở.
    private List<LocalDate> cacThangBaoCao() {
        List<LocalDate> dates = jdbc.getJdbcTemplate().queryForList("""
            SELECT NgayBatDau FROM O_PHONG
            UNION ALL SELECT CASE WHEN NgayKetThuc IS NULL THEN CURRENT_DATE()
                                  ELSE DATE_SUB(NgayKetThuc,INTERVAL 1 DAY) END FROM O_PHONG
            UNION ALL SELECT ThangDangKy FROM VE_THANG
            UNION ALL SELECT DATE(ThoiGianSD) FROM LAN_SU_DUNG
            UNION ALL SELECT DATE(ThoiGianGui) FROM LUOT_GUI_LAY_XE
            """, LocalDate.class);
        if (dates.isEmpty()) return List.of();
        LocalDate today = LocalDate.now();
        LocalDate first = dates.stream().filter(d -> d != null && !d.isAfter(today)).min(LocalDate::compareTo).orElse(null);
        if (first == null) return List.of();
        LocalDate last = dates.stream().filter(java.util.Objects::nonNull).max(LocalDate::compareTo).orElse(first);
        if (last.isAfter(today)) last = today;
        List<LocalDate> months = new ArrayList<>();
        for (LocalDate m = first.withDayOfMonth(1); !m.isAfter(last.withDayOfMonth(1)); m = m.plusMonths(1)) {
            if (months.size() >= 1200) throw com.baitapnhom.quanlyktx.exception.AppException.invalid("Dữ liệu quá 100 năm; hãy chọn một tháng cụ thể");
            months.add(m);
        }
        return months;
    }
}
