package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.CanhBaoDto;
import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.repository.ThongKeRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ThongKeService {

    private final ThongKeRepository thongKeRepository;
    private final TruyVanRepository truyVan;

    public ThongKeService(ThongKeRepository thongKeRepository, TruyVanRepository truyVan) {
        this.thongKeRepository = thongKeRepository;
        this.truyVan = truyVan;
    }

    // Các việc cần chú ý hôm nay: vé tháng hết hạn, phòng đầy, sinh viên chưa có phòng, xe gửi quá ngày.
    public CanhBaoDto.Response canhBao() {
        LocalDate homNay = LocalDate.now();
        List<PhongDto.PhongResponse> phongDay = truyVan.phongs(homNay).stream()
                .filter(p -> p.conCho() == 0)
                .toList();
        return new CanhBaoDto.Response(homNay,
                thongKeRepository.xeChuaCoVeThang(homNay.withDayOfMonth(1)),
                phongDay,
                thongKeRepository.sinhVienChuaCoPhong(homNay),
                thongKeRepository.sapHetHanO(homNay, homNay.plusDays(30)),
                thongKeRepository.xeGuiTuNgayTruoc(homNay.atStartOfDay()));
    }
}
