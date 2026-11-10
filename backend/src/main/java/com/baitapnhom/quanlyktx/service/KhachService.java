package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.KhachDto;
import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.entity.Khach;
import com.baitapnhom.quanlyktx.entity.LuotTham;
import com.baitapnhom.quanlyktx.entity.LuotThamId;
import com.baitapnhom.quanlyktx.entity.Nguoi;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.KhachRepository;
import com.baitapnhom.quanlyktx.repository.LuotThamRepository;
import com.baitapnhom.quanlyktx.repository.NguoiRepository;
import com.baitapnhom.quanlyktx.repository.SinhVienRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Khách đến chơi và các lượt thăm
@Service
public class KhachService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;

    private final NguoiRepository nguoiRepository;
    private final KhachRepository khachRepository;
    private final SinhVienRepository sinhVienRepository;
    private final LuotThamRepository luotThamRepository;
    private final TruyVanRepository truyVan;
    private final RangBuocService rangBuoc;
    private final NamedParameterJdbcTemplate jdbc;

    public KhachService(NguoiRepository nguoiRepository, KhachRepository khachRepository,
            SinhVienRepository sinhVienRepository, LuotThamRepository luotThamRepository,
            TruyVanRepository truyVan, RangBuocService rangBuoc, NamedParameterJdbcTemplate jdbc) {
        this.nguoiRepository = nguoiRepository;
        this.khachRepository = khachRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.luotThamRepository = luotThamRepository;
        this.truyVan = truyVan;
        this.rangBuoc = rangBuoc;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<KhachDto.Response> danhSach() {
        return truyVan.khachs();
    }

    @Transactional(readOnly = true)
    public List<KhachDto.LuotThamResponse> danhSachLuotTham(String maSV, LocalDate tuNgay, LocalDate denNgay) {
        return truyVan.luotTham(maSV, tuNgay, denNgay);
    }

    // Ghi nhận khách đến thăm sinh viên
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public KhachDto.LuotThamResponse ghiNhanLuotTham(KhachDto.LuotThamRequest req) {
        writeGuard.acquire();
        SinhVienDto.Response sv = truyVan.sinhVien(req.maSV())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên " + req.maSV()));
        LocalDateTime thoiGian = req.ngayDenChoi() != null ? req.ngayDenChoi() : LocalDateTime.now().withNano(0);
        rangBuoc.yeuCauDangOKtx(req.maSV(), thoiGian.toLocalDate(), "tiếp khách");
        String cccd = req.cccdKhach();
        Nguoi nguoi = nguoiRepository.findById(cccd).orElse(null);
        if (nguoi == null) {
            if (req.hoTen() == null || req.hoTen().isBlank() || req.ngaySinh() == null) {
                throw AppException.invalid("Khách mới cần nhập họ tên và ngày sinh");
            }
            nguoi = nguoiRepository.saveAndFlush(new Nguoi(cccd, req.hoTen().trim(), req.ngaySinh()));
            khachRepository.saveAndFlush(new Khach(cccd));
        } else if (sinhVienRepository.existsByCccd(cccd)) {
            throw AppException.rule("CCCD này thuộc về một sinh viên, không thể ghi nhận là khách");
        } else if (!khachRepository.existsById(cccd)) {
            khachRepository.saveAndFlush(new Khach(cccd));
        }
        if (luotThamRepository.existsById(new LuotThamId(cccd, req.maSV(), thoiGian))) {
            throw AppException.exists("Đã có lượt thăm này vào đúng thời điểm " + thoiGian);
        }
        luotThamRepository.saveAndFlush(new LuotTham(cccd, req.maSV(), thoiGian));
        return new KhachDto.LuotThamResponse(cccd, nguoi.getHoTen(), sv.maSV(), sv.hoTen(), thoiGian);
    }

    // Hủy một lượt khách đến thăm (khi nhập nhầm).
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void huyLuotTham(String cccdKhach, String maSV, LocalDateTime ngayDenChoi) {
        writeGuard.acquire();
        String formatted = ngayDenChoi.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int rows = jdbc.getJdbcTemplate().update(
                "DELETE FROM LUOT_THAM WHERE CCCD_Khach = ? AND MaSV = ? AND NgayDenChoi = ?",
                cccdKhach, maSV, formatted);

        if (rows == 0) {
            LuotThamId id = new LuotThamId(cccdKhach, maSV, ngayDenChoi.withNano(0));
            if (luotThamRepository.existsById(id)) {
                luotThamRepository.deleteById(id);
                return;
            }
            throw AppException.notFound("Không tìm thấy lượt thăm này");
        }
    }
}
