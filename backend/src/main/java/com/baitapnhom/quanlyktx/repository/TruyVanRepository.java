package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.dto.BaoCaoDto;
import com.baitapnhom.quanlyktx.dto.DichVuDto;
import com.baitapnhom.quanlyktx.dto.KhachDto;
import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.dto.XeDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class TruyVanRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TruyVanRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Sinh viên, khách
    static final String SQL_SINH_VIEN = """
            SELECT sv.MaSV, sv.CCCD, n.HoTen, n.NgaySinh, sv.Lop, sv.QueQuan
            FROM SINH_VIEN sv
            JOIN NGUOI n ON n.CCCD = sv.CCCD
            """;

    static final RowMapper<SinhVienDto.Response> SINH_VIEN = (rs, i) -> new SinhVienDto.Response(
            rs.getString("MaSV"), rs.getString("CCCD"), rs.getString("HoTen"),
            rs.getObject("NgaySinh", LocalDate.class), rs.getString("Lop"), rs.getString("QueQuan"));

    public List<SinhVienDto.Response> timSinhVien(String tuKhoa) {
        StringBuilder sql = new StringBuilder(SQL_SINH_VIEN);
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append(" WHERE sv.MaSV LIKE :kw OR n.HoTen LIKE :kw OR sv.Lop LIKE :kw OR sv.QueQuan LIKE :kw");
            p.addValue("kw", "%" + tuKhoa.trim() + "%");
        }
        sql.append(" ORDER BY sv.MaSV");
        return jdbc.query(sql.toString(), p, SINH_VIEN);
    }

    public Optional<SinhVienDto.Response> sinhVien(String maSV) {
        return jdbc.query(SQL_SINH_VIEN + " WHERE sv.MaSV = :maSV", new MapSqlParameterSource("maSV", maSV),
                SINH_VIEN).stream().findFirst();
    }

    public List<KhachDto.Response> khachs() {
        return jdbc.query("""
                SELECT k.CCCD, n.HoTen, n.NgaySinh
                FROM KHACH k
                JOIN NGUOI n ON n.CCCD = k.CCCD
                ORDER BY n.HoTen
                """, new MapSqlParameterSource(),
                (rs, i) -> new KhachDto.Response(rs.getString("CCCD"), rs.getString("HoTen"),
                        rs.getObject("NgaySinh", LocalDate.class)));
    }

    // Phòng, ở phòng
    private static final String SQL_PHONG = """
            SELECT p.SoPhong, p.LoaiPhong, p.DonGia, p.SoNguoiToiDa,
                   (SELECT COUNT(*) FROM O_PHONG o
                     WHERE o.SoPhong = p.SoPhong AND o.NgayBatDau <= :ngay
                       AND (o.NgayKetThuc IS NULL OR o.NgayKetThuc > :ngay)) AS so_nguoi_dang_o
            FROM PHONG p
            """;

    private static final RowMapper<PhongDto.PhongResponse> PHONG = (rs, i) -> {
        int toiDa = rs.getInt("SoNguoiToiDa");
        int dangO = rs.getInt("so_nguoi_dang_o");
        return new PhongDto.PhongResponse(rs.getString("SoPhong"), rs.getString("LoaiPhong"),
                rs.getBigDecimal("DonGia"), toiDa, dangO, Math.max(0, toiDa - dangO));
    };

    // Danh sách phòng kèm số người đang ở vào ngày đã cho.
    public List<PhongDto.PhongResponse> phongs(LocalDate ngay) {
        return jdbc.query(SQL_PHONG + " ORDER BY p.SoPhong", new MapSqlParameterSource("ngay", ngay), PHONG);
    }

    public Optional<PhongDto.PhongResponse> phong(String soPhong, LocalDate ngay) {
        MapSqlParameterSource p = new MapSqlParameterSource("ngay", ngay).addValue("soPhong", soPhong);
        return jdbc.query(SQL_PHONG + " WHERE p.SoPhong = :soPhong", p, PHONG).stream().findFirst();
    }

    static final String SQL_O_PHONG = """
            SELECT o.MaSV, n.HoTen, o.SoPhong, p.LoaiPhong, p.DonGia, o.NgayBatDau, o.NgayKetThuc
            FROM O_PHONG o
            JOIN SINH_VIEN sv ON sv.MaSV = o.MaSV
            JOIN NGUOI n ON n.CCCD = sv.CCCD
            JOIN PHONG p ON p.SoPhong = o.SoPhong
            """;

    static final RowMapper<PhongDto.OPhongResponse> O_PHONG = (rs, i) -> new PhongDto.OPhongResponse(
            rs.getString("MaSV"), rs.getString("HoTen"), rs.getString("SoPhong"), rs.getString("LoaiPhong"),
            rs.getBigDecimal("DonGia"), rs.getObject("NgayBatDau", LocalDate.class),
            rs.getObject("NgayKetThuc", LocalDate.class));

    public List<PhongDto.OPhongResponse> lichSuO(String maSV) {
        return jdbc.query(SQL_O_PHONG + " WHERE o.MaSV = :maSV ORDER BY o.NgayBatDau DESC",
                new MapSqlParameterSource("maSV", maSV), O_PHONG);
    }

    // Sinh viên đang ở trong phòng vào ngày đã cho.
    public List<PhongDto.OPhongResponse> dangO(String soPhong, LocalDate ngay) {
        MapSqlParameterSource p = new MapSqlParameterSource("soPhong", soPhong).addValue("ngay", ngay);
        return jdbc.query(SQL_O_PHONG + """
                 WHERE o.SoPhong = :soPhong AND o.NgayBatDau <= :ngay
                   AND (o.NgayKetThuc IS NULL OR o.NgayKetThuc > :ngay)
                 ORDER BY n.HoTen
                """, p, O_PHONG);
    }

    public Optional<PhongDto.OPhongResponse> chiTietO(String maSV, String soPhong, LocalDate ngayBatDau) {
        MapSqlParameterSource p = new MapSqlParameterSource("maSV", maSV).addValue("soPhong", soPhong)
                .addValue("ngayBatDau", ngayBatDau);
        return jdbc.query(SQL_O_PHONG
                + " WHERE o.MaSV = :maSV AND o.SoPhong = :soPhong AND o.NgayBatDau = :ngayBatDau", p, O_PHONG)
                .stream().findFirst();
    }

    // Dịch vụ
    private static final String SQL_SU_DUNG = """
            SELECT l.MaSV, n.HoTen, l.MaDV, d.TenDV, l.ThoiGianSD, l.SoLuong, d.DonGia,
                   (l.SoLuong * d.DonGia) AS thanh_tien
            FROM LAN_SU_DUNG l
            JOIN SINH_VIEN sv ON sv.MaSV = l.MaSV
            JOIN NGUOI n ON n.CCCD = sv.CCCD
            JOIN DICH_VU d ON d.MaDV = l.MaDV
            WHERE 1 = 1
            """;

    // Các lần sử dụng dịch vụ
    public List<DichVuDto.SuDungResponse> suDungDichVu(String maSV, LocalDate tuNgay, LocalDate denNgay) {
        StringBuilder sql = new StringBuilder(SQL_SU_DUNG);
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (maSV != null && !maSV.isBlank()) {
            sql.append(" AND l.MaSV = :maSV");
            p.addValue("maSV", maSV);
        }
        if (tuNgay != null) {
            sql.append(" AND l.ThoiGianSD >= :tu");
            p.addValue("tu", tuNgay.atStartOfDay());
        }
        if (denNgay != null) {
            sql.append(" AND l.ThoiGianSD < :den");
            p.addValue("den", denNgay.plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY l.ThoiGianSD DESC");
        return jdbc.query(sql.toString(), p, (rs, i) -> new DichVuDto.SuDungResponse(rs.getString("MaSV"),
                rs.getString("HoTen"), rs.getString("MaDV"), rs.getString("TenDV"),
                rs.getObject("ThoiGianSD", LocalDateTime.class), rs.getInt("SoLuong"),
                rs.getBigDecimal("DonGia"), rs.getBigDecimal("thanh_tien")));
    }

    // Khách đến thăm

    public List<KhachDto.LuotThamResponse> luotTham(String maSV, LocalDate tuNgay, LocalDate denNgay) {
        StringBuilder sql = new StringBuilder("""
                SELECT lt.CCCD_Khach, nk.HoTen AS ho_ten_khach, lt.MaSV, ns.HoTen AS ho_ten_sv, lt.NgayDenChoi
                FROM LUOT_THAM lt
                JOIN KHACH k ON k.CCCD = lt.CCCD_Khach
                JOIN NGUOI nk ON nk.CCCD = k.CCCD
                JOIN SINH_VIEN sv ON sv.MaSV = lt.MaSV
                JOIN NGUOI ns ON ns.CCCD = sv.CCCD
                WHERE 1 = 1
                """);
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (maSV != null && !maSV.isBlank()) {
            sql.append(" AND lt.MaSV = :maSV");
            p.addValue("maSV", maSV);
        }
        if (tuNgay != null) {
            sql.append(" AND lt.NgayDenChoi >= :tu");
            p.addValue("tu", tuNgay.atStartOfDay());
        }
        if (denNgay != null) {
            sql.append(" AND lt.NgayDenChoi < :den");
            p.addValue("den", denNgay.plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY lt.NgayDenChoi DESC");
        return jdbc.query(sql.toString(), p, (rs, i) -> new KhachDto.LuotThamResponse(rs.getString("CCCD_Khach"),
                rs.getString("ho_ten_khach"), rs.getString("MaSV"), rs.getString("ho_ten_sv"),
                rs.getObject("NgayDenChoi", LocalDateTime.class)));
    }

    // Xe, vé tháng, lượt gửi/lấy
    static final String SQL_XE = """
            SELECT x.BienSo, x.LoaiXe, x.MaSV, n.HoTen
            FROM XE x
            JOIN SINH_VIEN sv ON sv.MaSV = x.MaSV
            JOIN NGUOI n ON n.CCCD = sv.CCCD
            """;

    static final RowMapper<XeDto.XeResponse> XE = (rs, i) -> new XeDto.XeResponse(
            rs.getString("BienSo"), rs.getString("LoaiXe"), rs.getString("MaSV"), rs.getString("HoTen"));

    public List<XeDto.XeResponse> xes(String maSV) {
        if (maSV == null || maSV.isBlank()) {
            return jdbc.query(SQL_XE + " ORDER BY x.BienSo", new MapSqlParameterSource(), XE);
        }
        return jdbc.query(SQL_XE + " WHERE x.MaSV = :maSV ORDER BY x.BienSo",
                new MapSqlParameterSource("maSV", maSV), XE);
    }

    public Optional<XeDto.XeResponse> xe(String bienSo) {
        return jdbc.query(SQL_XE + " WHERE x.BienSo = :bienSo", new MapSqlParameterSource("bienSo", bienSo), XE)
                .stream().findFirst();
    }

    public List<XeDto.VeThangResponse> veThangs(String bienSo) {
        StringBuilder sql = new StringBuilder("""
                SELECT BienSo, DATE_FORMAT(ThangDangKy, '%Y-%m') AS thang, DonGiaVe
                FROM VE_THANG
                WHERE 1 = 1
                """);
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (bienSo != null && !bienSo.isBlank()) {
            sql.append(" AND BienSo = :bienSo");
            p.addValue("bienSo", bienSo);
        }
        sql.append(" ORDER BY ThangDangKy DESC, BienSo");
        return jdbc.query(sql.toString(), p, (rs, i) -> new XeDto.VeThangResponse(rs.getString("BienSo"),
                rs.getString("thang"), rs.getBigDecimal("DonGiaVe")));
    }

    // Các lượt gửi/lấy xe; bienSo và ngay có thể để null (không lọc).
    public List<XeDto.LuotResponse> luotGuiLay(String bienSo, LocalDate ngay) {
        StringBuilder sql = new StringBuilder("""
                SELECT BienSo, ThoiGianGui, ThoiGianLay, SoTienPhaiTra
                FROM V_LUOT_GUI_LAY_XE
                WHERE 1 = 1
                """);
        MapSqlParameterSource p = new MapSqlParameterSource();
        if (bienSo != null && !bienSo.isBlank()) {
            sql.append(" AND BienSo = :bienSo");
            p.addValue("bienSo", bienSo);
        }
        if (ngay != null) {
            sql.append(" AND ThoiGianGui >= :tu AND ThoiGianGui < :den");
            p.addValue("tu", ngay.atStartOfDay());
            p.addValue("den", ngay.plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY ThoiGianGui DESC");
        return jdbc.query(sql.toString(), p, (rs, i) -> new XeDto.LuotResponse(rs.getString("BienSo"),
                rs.getObject("ThoiGianGui", LocalDateTime.class), rs.getObject("ThoiGianLay", LocalDateTime.class),
                rs.getBigDecimal("SoTienPhaiTra")));
    }

    // Thống kê tổng quan
    public BaoCaoDto.TongQuan tongQuan(LocalDate ngay) {
        LocalDate dauThang = ngay.withDayOfMonth(1);
        MapSqlParameterSource p = new MapSqlParameterSource("ngay", ngay).addValue("thang", dauThang)
                .addValue("thangSau", dauThang.plusMonths(1));
        return jdbc.queryForObject("""
                SELECT
                  (SELECT COUNT(*) FROM SINH_VIEN) AS so_sinh_vien,
                  (SELECT COUNT(*) FROM PHONG) AS so_phong,
                  (SELECT COALESCE(SUM(SoNguoiToiDa), 0) FROM PHONG) AS tong_cho,
                  (SELECT COUNT(*) FROM O_PHONG
                    WHERE NgayBatDau <= :ngay AND (NgayKetThuc IS NULL OR NgayKetThuc > :ngay)) AS dang_o,
                  (SELECT COUNT(*) FROM XE) AS so_xe,
                  (SELECT COUNT(*) FROM VE_THANG WHERE ThangDangKy = :thang) AS ve_thang_nay,
                  (SELECT COUNT(*) FROM LUOT_THAM
                    WHERE NgayDenChoi >= :thang AND NgayDenChoi < :thangSau) AS khach_thang_nay
                """, p, (rs, i) -> {
                    int tongCho = rs.getInt("tong_cho");
                    int dangO = rs.getInt("dang_o");
                    return new BaoCaoDto.TongQuan(rs.getInt("so_sinh_vien"), rs.getInt("so_phong"), tongCho, dangO,
                            Math.max(0, tongCho - dangO), rs.getInt("so_xe"), rs.getInt("ve_thang_nay"),
                            rs.getInt("khach_thang_nay"), BigDecimal.ZERO);
                });
    }
}
