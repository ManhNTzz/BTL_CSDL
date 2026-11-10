package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.dto.XeDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

// Các truy vấn phục vụ cảnh báo/nhắc việc cho quản lý KTX.
@Repository
public class ThongKeRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ThongKeRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Xe chưa có vé tháng của tháng đã cho (vé hết hạn, cần gia hạn).
    public List<XeDto.XeResponse> xeChuaCoVeThang(LocalDate dauThang) {
        return jdbc.query(TruyVanRepository.SQL_XE + """
                 WHERE NOT EXISTS (SELECT 1 FROM VE_THANG v
                                    WHERE v.BienSo = x.BienSo AND v.ThangDangKy = :thang)
                 ORDER BY x.BienSo
                """, new MapSqlParameterSource("thang", dauThang), TruyVanRepository.XE);
    }

    // Sinh viên hiện không ở phòng nào.
    public List<SinhVienDto.Response> sinhVienChuaCoPhong(LocalDate ngay) {
        return jdbc.query(TruyVanRepository.SQL_SINH_VIEN + """
                 WHERE NOT EXISTS (SELECT 1 FROM O_PHONG o
                                    WHERE o.MaSV = sv.MaSV AND o.NgayBatDau <= :ngay
                                      AND (o.NgayKetThuc IS NULL OR o.NgayKetThuc > :ngay))
                 ORDER BY sv.MaSV
                """, new MapSqlParameterSource("ngay", ngay), TruyVanRepository.SINH_VIEN);
    }

    // Các bản ghi ở phòng sẽ kết thúc trong khoảng [tuNgay, denNgay].
    public List<PhongDto.OPhongResponse> sapHetHanO(LocalDate tuNgay, LocalDate denNgay) {
        MapSqlParameterSource p = new MapSqlParameterSource("tu", tuNgay).addValue("den", denNgay);
        return jdbc.query(TruyVanRepository.SQL_O_PHONG + """
                 WHERE o.NgayKetThuc IS NOT NULL AND o.NgayKetThuc >= :tu AND o.NgayKetThuc <= :den
                 ORDER BY o.NgayKetThuc
                """, p, TruyVanRepository.O_PHONG);
    }

    // Xe gửi từ trước thời điểm đã cho mà vẫn chưa lấy ra.
    public List<XeDto.LuotResponse> xeGuiTuNgayTruoc(LocalDateTime moc) {
        return jdbc.query("""
                SELECT BienSo, ThoiGianGui, ThoiGianLay, SoTienPhaiTra
                FROM V_LUOT_GUI_LAY_XE
                WHERE ThoiGianLay IS NULL AND ThoiGianGui < :moc
                ORDER BY ThoiGianGui
                """, new MapSqlParameterSource("moc", moc),
                (rs, i) -> new XeDto.LuotResponse(rs.getString("BienSo"),
                        rs.getObject("ThoiGianGui", LocalDateTime.class),
                        rs.getObject("ThoiGianLay", LocalDateTime.class), rs.getBigDecimal("SoTienPhaiTra")));
    }
}
