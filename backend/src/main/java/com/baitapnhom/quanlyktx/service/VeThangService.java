package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.XeDto;
import com.baitapnhom.quanlyktx.entity.VeThang;
import com.baitapnhom.quanlyktx.entity.VeThangId;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import com.baitapnhom.quanlyktx.repository.VeThangRepository;
import com.baitapnhom.quanlyktx.repository.XeRepository;
import com.baitapnhom.quanlyktx.util.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Đăng ký vé gửi xe tháng, đơn giá mặc định 100.000đ/tháng.
@Service
public class VeThangService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;

    public static final BigDecimal GIA_VE_THANG = new BigDecimal("100000");

    private final VeThangRepository veThangRepository;
    private final XeRepository xeRepository;
    private final TruyVanRepository truyVan;

    public VeThangService(VeThangRepository veThangRepository, XeRepository xeRepository,
            TruyVanRepository truyVan) {
        this.veThangRepository = veThangRepository;
        this.xeRepository = xeRepository;
        this.truyVan = truyVan;
    }

    @Transactional(readOnly = true)
    public List<XeDto.VeThangResponse> danhSach(String bienSo) {
        return truyVan.veThangs(bienSo);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public XeDto.VeThangResponse dangKy(XeDto.VeThangRequest req) {
        writeGuard.acquire();
        YearMonth thang = DateUtil.parseThang(req.thang());
        if (!xeRepository.existsById(req.bienSo())) {
            throw AppException.notFound("Không tìm thấy xe " + req.bienSo());
        }
        LocalDate ngayDau = thang.atDay(1);
        if (veThangRepository.existsById(new VeThangId(req.bienSo(), ngayDau))) {
            throw AppException.exists("Xe " + req.bienSo() + " đã đăng ký vé tháng " + thang);
        }
        if (req.donGiaVe() != null && req.donGiaVe().compareTo(GIA_VE_THANG) != 0) {
            throw AppException.rule("Giá vé tháng cố định 100.000 đồng/xe/tháng");
        }
        BigDecimal gia = GIA_VE_THANG;
        veThangRepository.saveAndFlush(new VeThang(req.bienSo(), ngayDau, gia));
        return new XeDto.VeThangResponse(req.bienSo(), thang.toString(), gia);
    }

    // Gia hạn vé cho tháng đã cho: mọi xe có vé tháng trước mà chưa có vé tháng này sẽ được đăng ký tiếp.
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public XeDto.GiaHanResponse giaHan(String thangText) {
        writeGuard.acquire();
        YearMonth thang = DateUtil.parseThang(thangText);
        LocalDate ngayDau = thang.atDay(1);
        int daTao = 0;
        int daCoSan = 0;
        for (VeThang v : veThangRepository.findByThangDangKy(thang.minusMonths(1).atDay(1))) {
            if (veThangRepository.existsById(new VeThangId(v.getBienSo(), ngayDau))) {
                daCoSan++;
            } else {
                veThangRepository.save(new VeThang(v.getBienSo(), ngayDau, GIA_VE_THANG));
                daTao++;
            }
        }
        veThangRepository.flush();
        return new XeDto.GiaHanResponse(thang.toString(), daTao, daCoSan);
    }
}
