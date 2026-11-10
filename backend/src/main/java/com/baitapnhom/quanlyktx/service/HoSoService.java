package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.dto.XeDto;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.BaoCaoRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Tra cứu hồ sơ đầy đủ của một sinh viên (để sinh viên/quản lý xem hóa đơn và lịch sử của một người).
@Service
public class HoSoService {

        private final TruyVanRepository truyVan;
        private final BaoCaoRepository baoCaoRepository;

        public HoSoService(TruyVanRepository truyVan, BaoCaoRepository baoCaoRepository) {
                this.truyVan = truyVan;
                this.baoCaoRepository = baoCaoRepository;
        }

        @Transactional(readOnly = true)
        public SinhVienDto.HoSo hoSo(String maSV) {
                SinhVienDto.Response sv = truyVan.sinhVien(maSV)
                                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên " + maSV));
                LocalDate homNay = LocalDate.now();
                List<PhongDto.OPhongResponse> lichSu = truyVan.lichSuO(maSV);
                PhongDto.OPhongResponse hienTai = lichSu.stream()
                                .filter(o -> !o.ngayBatDau().isAfter(homNay)
                                                && (o.ngayKetThuc() == null || o.ngayKetThuc().isAfter(homNay)))
                                .findFirst()
                                .orElse(null);
                List<XeDto.XeResponse> xes = truyVan.xes(maSV);
                List<XeDto.VeThangResponse> veThang = xes.stream()
                                .flatMap(x -> truyVan.veThangs(x.bienSo()).stream())
                                .toList();
                return new SinhVienDto.HoSo(sv, hienTai, lichSu, xes, veThang,
                                truyVan.suDungDichVu(maSV, null, null), truyVan.luotTham(maSV, null, null),
                                baoCaoRepository.hoaDon(null, maSV));
        }
}
