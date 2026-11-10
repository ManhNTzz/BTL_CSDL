package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.XeDto;
import com.baitapnhom.quanlyktx.entity.Xe;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.SinhVienRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import com.baitapnhom.quanlyktx.repository.XeRepository;
import java.time.YearMonth;
import com.baitapnhom.quanlyktx.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Xe gửi vé tháng của sinh viên, mỗi sinh viên tối đa 2 xe.
@Service
public class XeService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;
    @Autowired private JdbcTemplate jdbc;

    public static final int SO_XE_TOI_DA = 2;

    private final XeRepository xeRepository;
    private final SinhVienRepository sinhVienRepository;
    private final TruyVanRepository truyVan;
    private final RangBuocService rangBuoc;

    public XeService(XeRepository xeRepository, SinhVienRepository sinhVienRepository, TruyVanRepository truyVan,
            RangBuocService rangBuoc) {
        this.xeRepository = xeRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.truyVan = truyVan;
        this.rangBuoc = rangBuoc;
    }

    @Transactional(readOnly = true)
    public List<XeDto.XeResponse> danhSach(String maSV) {
        return truyVan.xes(maSV);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public XeDto.XeResponse dangKy(XeDto.XeRequest req) {
        writeGuard.acquire();
        String bienSo = req.bienSo().trim();
        if (!sinhVienRepository.existsById(req.maSV())) {
            throw AppException.notFound("Không tìm thấy sinh viên " + req.maSV());
        }
        YearMonth thang = req.thang() == null || req.thang().isBlank() ? YearMonth.now()
                : DateUtil.parseThang(req.thang());
        rangBuoc.yeuCauCoDotOTrongThang(req.maSV(), thang);
        if (xeRepository.existsById(bienSo)) {
            throw AppException.exists("Xe " + bienSo + " đã được đăng ký");
        }
        if (xeRepository.countByMaSV(req.maSV()) >= SO_XE_TOI_DA) {
            throw AppException.rule("Mỗi sinh viên chỉ được đăng ký tối đa " + SO_XE_TOI_DA + " xe vé tháng");
        }
        xeRepository.saveAndFlush(new Xe(bienSo, req.loaiXe().trim(), req.maSV()));
        jdbc.update("INSERT INTO VE_THANG (BienSo,ThangDangKy,DonGiaVe) VALUES (?,?,100000)", bienSo, thang.atDay(1));
        return truyVan.xe(bienSo).orElseThrow(() -> AppException.notFound("Không tìm thấy xe " + bienSo));
    }
    
    // Chỉ xóa xe chưa có lịch sử vé
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void xoa(String bienSo) {
        writeGuard.acquire();
        if (!xeRepository.existsById(bienSo)) {
            throw AppException.notFound("Không tìm thấy xe " + bienSo);
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM VE_THANG WHERE BienSo=?", Long.class, bienSo) > 0) {
            throw AppException.rule("Xe đã có vé tháng; không xóa để tránh mất lịch sử và tiền vé");
        }
        xeRepository.deleteById(bienSo);
        xeRepository.flush();
    }
}
