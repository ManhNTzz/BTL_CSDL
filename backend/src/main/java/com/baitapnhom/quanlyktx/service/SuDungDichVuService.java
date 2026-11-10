package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.DichVuDto;
import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.entity.DichVu;
import com.baitapnhom.quanlyktx.entity.LanSuDung;
import com.baitapnhom.quanlyktx.entity.LanSuDungId;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.DichVuRepository;
import com.baitapnhom.quanlyktx.repository.LanSuDungRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Ghi nhận mỗi lần sinh viên sử dụng một dịch vụ
@Service
public class SuDungDichVuService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;

    private final LanSuDungRepository lanSuDungRepository;
    private final DichVuRepository dichVuRepository;
    private final TruyVanRepository truyVan;
    private final RangBuocService rangBuoc;
    private final NamedParameterJdbcTemplate jdbc;

    public SuDungDichVuService(LanSuDungRepository lanSuDungRepository, DichVuRepository dichVuRepository,
            TruyVanRepository truyVan, RangBuocService rangBuoc, NamedParameterJdbcTemplate jdbc) {
        this.lanSuDungRepository = lanSuDungRepository;
        this.dichVuRepository = dichVuRepository;
        this.truyVan = truyVan;
        this.rangBuoc = rangBuoc;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<DichVuDto.SuDungResponse> danhSach(String maSV, LocalDate tuNgay, LocalDate denNgay) {
        return truyVan.suDungDichVu(maSV, tuNgay, denNgay);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public DichVuDto.SuDungResponse ghiNhan(DichVuDto.SuDungRequest req) {
        writeGuard.acquire();
        SinhVienDto.Response sv = truyVan.sinhVien(req.maSV())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên " + req.maSV()));
        DichVu dv = dichVuRepository.findById(req.maDV())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy dịch vụ " + req.maDV()));

        if ("Trông xe".equalsIgnoreCase(dv.getTenDV())) {
            throw AppException.rule("Dịch vụ Trông xe của sinh viên được quản lý qua Vé tháng và Lượt gửi/lấy xe, không ghi vào lần sử dụng");
        }

        LocalDateTime thoiGian = (req.thoiGianSD() != null ? req.thoiGianSD() : LocalDateTime.now()).withNano(0);

        rangBuoc.yeuCauDangOKtx(req.maSV(), thoiGian.toLocalDate(), "sử dụng dịch vụ");
        if (lanSuDungRepository.existsById(new LanSuDungId(req.maSV(), req.maDV(), thoiGian))) {
            throw AppException.exists("Đã có bản ghi sử dụng dịch vụ này vào đúng thời điểm " + thoiGian);
        }
        lanSuDungRepository.saveAndFlush(new LanSuDung(req.maSV(), req.maDV(), thoiGian, req.soLuong()));
        BigDecimal thanhTien = dv.getDonGia().multiply(BigDecimal.valueOf(req.soLuong()));
        return new DichVuDto.SuDungResponse(sv.maSV(), sv.hoTen(), dv.getMaDV(), dv.getTenDV(), thoiGian,
                req.soLuong(), dv.getDonGia(), thanhTien);
    }

    // Hủy một lần ghi nhận sử dụng dịch vụ (khi nhập nhầm).
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void huy(String maSV, String maDV, LocalDateTime thoiGianSD) {
        writeGuard.acquire();
        String formatted = thoiGianSD.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int rows = jdbc.getJdbcTemplate().update(
                "DELETE FROM LAN_SU_DUNG WHERE MaSV = ? AND MaDV = ? AND ThoiGianSD = ?",
                maSV, maDV, formatted);

        if (rows == 0) {
            LanSuDungId id = new LanSuDungId(maSV, maDV, thoiGianSD.withNano(0));
            if (lanSuDungRepository.existsById(id)) {
                lanSuDungRepository.deleteById(id);
                return;
            }
            throw AppException.notFound("Không tìm thấy lần sử dụng dịch vụ này");
        }
    }
}