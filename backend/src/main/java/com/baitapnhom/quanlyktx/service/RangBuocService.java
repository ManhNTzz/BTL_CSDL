package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.OPhongRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// chỉ sinh viên đang ở trong KTX mới được sử dụng dịch vụ, gửi xe, tiếp khách.
@Service
public class RangBuocService {

    private final OPhongRepository oPhongRepository;

    public RangBuocService(OPhongRepository oPhongRepository) {
        this.oPhongRepository = oPhongRepository;
    }

    @Transactional(readOnly = true)
    public boolean dangOKtx(String maSV, LocalDate ngay) {
        return oPhongRepository.findByMaSV(maSV).stream()
                .anyMatch(o -> !o.getNgayBatDau().isAfter(ngay)
                        && (o.getNgayKetThuc() == null || o.getNgayKetThuc().isAfter(ngay)));
    }

    @Transactional(readOnly = true)
    public void yeuCauDangOKtx(String maSV, LocalDate ngay, String hanhDong) {
        if (!dangOKtx(maSV, ngay)) {
            throw AppException.rule("Sinh viên " + maSV + " không ở trong KTX vào ngày " + ngay
                    + " nên không thể " + hanhDong);
        }
    }

    @Transactional(readOnly = true)
    public void yeuCauCoDotOTrongThang(String maSV, java.time.YearMonth thang) {
        LocalDate dau = thang.atDay(1), sau = thang.plusMonths(1).atDay(1);
        boolean coDot = oPhongRepository.findByMaSV(maSV).stream().anyMatch(o ->
                o.getNgayBatDau().isBefore(sau) && (o.getNgayKetThuc() == null || o.getNgayKetThuc().isAfter(dau)));
        if (!coDot) throw AppException.rule("Sinh viên không có đợt ở giao với tháng đăng ký vé");
    }
}
