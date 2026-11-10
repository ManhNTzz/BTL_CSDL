package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.entity.Nguoi;
import com.baitapnhom.quanlyktx.entity.SinhVien;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.NguoiRepository;
import com.baitapnhom.quanlyktx.repository.SinhVienRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SinhVienService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private final NguoiRepository nguoiRepository;
    private final SinhVienRepository sinhVienRepository;
    private final TruyVanRepository truyVan;

    public SinhVienService(NguoiRepository nguoiRepository, SinhVienRepository sinhVienRepository,
            TruyVanRepository truyVan) {
        this.nguoiRepository = nguoiRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.truyVan = truyVan;
    }

    @Transactional(readOnly = true)
    public List<SinhVienDto.Response> timKiem(String tuKhoa) {
        return truyVan.timSinhVien(tuKhoa);
    }

    @Transactional(readOnly = true)
    public SinhVienDto.Response chiTiet(String maSV) {
        return truyVan.sinhVien(maSV)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên " + maSV));
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public SinhVienDto.Response them(SinhVienDto.Request req) {
        writeGuard.acquire();
        String maSV = req.maSV().trim();
        if (sinhVienRepository.existsById(maSV)) {
            throw AppException.exists("Mã sinh viên " + maSV + " đã tồn tại");
        }
        if (nguoiRepository.existsById(req.cccd())) {
            throw AppException.exists("CCCD " + req.cccd() + " đã tồn tại (đã là sinh viên hoặc là khách)");
        }
        nguoiRepository.saveAndFlush(new Nguoi(req.cccd(), req.hoTen().trim(), req.ngaySinh()));
        sinhVienRepository.saveAndFlush(new SinhVien(maSV, req.cccd(), req.lop().trim(), req.queQuan().trim()));
        return new SinhVienDto.Response(maSV, req.cccd(), req.hoTen().trim(), req.ngaySinh(), req.lop().trim(),
                req.queQuan().trim());
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public SinhVienDto.Response capNhat(String maSV, SinhVienDto.UpdateRequest req) {
        writeGuard.acquire();
        SinhVien sv = sinhVienRepository.findById(maSV)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên " + maSV));
        Nguoi nguoi = nguoiRepository.findById(sv.getCccd())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy thông tin người của sinh viên " + maSV));
        nguoi.setHoTen(req.hoTen().trim());
        nguoi.setNgaySinh(req.ngaySinh());
        sv.setLop(req.lop().trim());
        sv.setQueQuan(req.queQuan().trim());
        nguoiRepository.saveAndFlush(nguoi);
        sinhVienRepository.saveAndFlush(sv);
        return new SinhVienDto.Response(maSV, sv.getCccd(), nguoi.getHoTen(), nguoi.getNgaySinh(), sv.getLop(),
                sv.getQueQuan());
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void xoa(String maSV) {
        writeGuard.acquire();
        SinhVien sv = sinhVienRepository.findById(maSV)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên " + maSV));
        long history = jdbc.queryForObject("SELECT (SELECT COUNT(*) FROM O_PHONG WHERE MaSV=?) + (SELECT COUNT(*) FROM XE WHERE MaSV=?) + (SELECT COUNT(*) FROM LAN_SU_DUNG WHERE MaSV=?) + (SELECT COUNT(*) FROM LUOT_THAM WHERE MaSV=?)", Long.class, maSV,maSV,maSV,maSV);
        if (history > 0) throw AppException.rule("Sinh viên đã có lịch sử lưu trú/giao dịch; không xóa để bảo toàn dữ liệu");
        try {
            sinhVienRepository.delete(sv);
            sinhVienRepository.flush();
            nguoiRepository.deleteById(sv.getCccd());
            nguoiRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw AppException.rule("Không thể xóa sinh viên " + maSV
                    + " vì còn dữ liệu liên quan (phòng đã ở, xe đã đăng ký)");
        }
    }
}
