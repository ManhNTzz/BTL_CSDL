package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.BaoCaoDto;
import com.baitapnhom.quanlyktx.repository.BaoCaoRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import com.baitapnhom.quanlyktx.util.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 4 truy vấn của đề bài và thống kê tổng qua
@Service
@Transactional(readOnly = true)
public class BaoCaoService {

    private final BaoCaoRepository baoCaoRepository;
    private final TruyVanRepository truyVan;

    public BaoCaoService(BaoCaoRepository baoCaoRepository, TruyVanRepository truyVan) {
        this.baoCaoRepository = baoCaoRepository;
        this.truyVan = truyVan;
    }

    // Truy vấn 1
    public List<BaoCaoDto.HoaDonThang> hoaDon(String thang, String maSV) {
        LocalDate ngayDau = thang == null || thang.isBlank() ? null : DateUtil.parseThang(thang).atDay(1);
        return baoCaoRepository.hoaDon(ngayDau, maSV);
    }

    // Truy vấn 2
    public List<BaoCaoDto.DichVuSinhVien> dichVuSinhVien(LocalDate tuNgay, LocalDate denNgay) {
        DateUtil.kiemTraKhoang(tuNgay, denNgay);
        return baoCaoRepository.dichVuSinhVien(tuNgay, denNgay);
    }

    // Truy vấn 3
    public List<BaoCaoDto.KhachTham> khachTham(String kieu, LocalDate ngay, LocalDate tuNgay, LocalDate denNgay) {
        LocalDate tu = tuNgay;
        LocalDate den = denNgay;
        if (tu == null && den == null && kieu != null && !kieu.isBlank()) {
            if (!"THANG".equalsIgnoreCase(kieu.trim()) && !"TUAN".equalsIgnoreCase(kieu.trim()))
                throw com.baitapnhom.quanlyktx.exception.AppException.invalid("Kiểu xem phải là TUAN hoặc THANG");
            LocalDate moc = ngay != null ? ngay : LocalDate.now();
            LocalDate[] khoang = "THANG".equalsIgnoreCase(kieu.trim()) ? DateUtil.khoangThang(moc)
                    : DateUtil.khoangTuan(moc);
            tu = khoang[0];
            den = khoang[1];
        }
        DateUtil.kiemTraKhoang(tu, den);
        return baoCaoRepository.khachTham(tu, den);
    }

    // Truy vấn 4
    public List<BaoCaoDto.DoanhThuDichVu> doanhThuDichVu(String thang) {
        LocalDate ngayDau = thang == null || thang.isBlank() ? null : DateUtil.parseThang(thang).atDay(1);
        return baoCaoRepository.doanhThuDichVu(ngayDau);
    }

    // Tổng thu mỗi tháng của KTX (tiền phòng, dịch vụ, vé tháng, phát sinh), dùng vẽ biểu đồ.
    public List<BaoCaoDto.DoanhThuThang> doanhThuTheoThang() {
        Map<String, BigDecimal[]> theoThang = new LinkedHashMap<>();
        for (BaoCaoDto.HoaDonThang h : baoCaoRepository.hoaDon(null, null)) {
            BigDecimal[] t = theoThang.computeIfAbsent(h.thang(),
                    k -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            t[0] = t[0].add(h.tienPhong());
            t[1] = t[1].add(h.tienDichVu());
            t[2] = t[2].add(h.tienVeThang());
            t[3] = t[3].add(h.tienPhatSinhXe());
        }
        return theoThang.entrySet().stream()
                .map(e -> {
                    BigDecimal[] t = e.getValue();
                    return new BaoCaoDto.DoanhThuThang(e.getKey(), t[0], t[1], t[2], t[3],
                            t[0].add(t[1]).add(t[2]).add(t[3]));
                })
                .toList();
    }

    public BaoCaoDto.TongQuan tongQuan() {
        BaoCaoDto.TongQuan t = truyVan.tongQuan(LocalDate.now());
        LocalDate dauThang = YearMonth.now().atDay(1);
        BigDecimal doanhThu = baoCaoRepository.hoaDon(dauThang, null).stream()
                .map(BaoCaoDto.HoaDonThang::tongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new BaoCaoDto.TongQuan(t.soSinhVien(), t.soPhong(), t.tongSoCho(), t.soNguoiDangO(), t.soChoTrong(),
                t.soXeVeThang(), t.soVeThangThangNay(), t.soLuotKhachThangNay(), doanhThu);
    }
}
