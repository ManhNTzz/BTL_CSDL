package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.entity.OPhong;
import com.baitapnhom.quanlyktx.entity.OPhongId;
import com.baitapnhom.quanlyktx.entity.Phong;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.OPhongRepository;
import com.baitapnhom.quanlyktx.repository.PhongRepository;
import com.baitapnhom.quanlyktx.repository.SinhVienRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Sinh viên ở phòng
@Service
public class OPhongService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;

    private static final LocalDate VO_CUC = LocalDate.of(9999, 12, 31);

    private final OPhongRepository oPhongRepository;
    private final SinhVienRepository sinhVienRepository;
    private final PhongRepository phongRepository;
    private final TruyVanRepository truyVan;

    public OPhongService(OPhongRepository oPhongRepository, SinhVienRepository sinhVienRepository,
            PhongRepository phongRepository, TruyVanRepository truyVan) {
        this.oPhongRepository = oPhongRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.phongRepository = phongRepository;
        this.truyVan = truyVan;
    }

    @Transactional(readOnly = true)
    public List<PhongDto.OPhongResponse> lichSu(String maSV) {
        return truyVan.lichSuO(maSV);
    }

    @Transactional(readOnly = true)
    public List<PhongDto.OPhongResponse> dangO(String soPhong, LocalDate ngay) {
        return truyVan.dangO(soPhong, ngay != null ? ngay : LocalDate.now());
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public PhongDto.OPhongResponse dangKy(PhongDto.OPhongRequest req) {
        writeGuard.acquire();
        OPhong o = kiemTraVaLuu(req.maSV(), req.soPhong(), req.ngayBatDau(), req.ngayKetThuc());
        return chiTiet(o);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public PhongDto.OPhongResponse ketThuc(PhongDto.KetThucRequest req) {
        writeGuard.acquire();
        OPhong o = oPhongRepository.findById(new OPhongId(req.maSV(), req.soPhong(), req.ngayBatDau()))
                .orElseThrow(() -> AppException.notFound("Không tìm thấy bản ghi ở phòng này"));
        if (!req.ngayKetThuc().isAfter(o.getNgayBatDau())) {
            throw AppException
                    .invalid("Ngày kết thúc phải lớn hơn ngày bắt đầu theo quy ước [NgayBatDau, NgayKetThuc)");
        }
        o.setNgayKetThuc(req.ngayKetThuc());
        oPhongRepository.saveAndFlush(o);
        return chiTiet(o);
    }

    // Chuyển phòng: đợt cũ kết thúc ngày chuyển, đợt mới bắt đầu ngày chuyển.
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public PhongDto.OPhongResponse chuyenPhong(PhongDto.ChuyenPhongRequest req) {
        writeGuard.acquire();
        OPhong hienTai = oPhongRepository.findByMaSV(req.maSV()).stream()
                .filter(o -> !o.getNgayBatDau().isAfter(req.ngayChuyen()) && dangO(o, req.ngayChuyen()))
                .max(Comparator.comparing(OPhong::getNgayBatDau))
                .orElseThrow(() -> AppException.notFound(
                        "Sinh viên chưa ở phòng nào vào ngày " + req.ngayChuyen()));
        if (hienTai.getSoPhong().equals(req.soPhongMoi())) {
            throw AppException.invalid("Sinh viên đang ở phòng này rồi");
        }
        LocalDate ketThucCu = req.ngayChuyen();
        if (!ketThucCu.isAfter(hienTai.getNgayBatDau())) {
            throw AppException.invalid("Ngày chuyển phải lớn hơn ngày bắt đầu ở phòng cũ");
        }
        hienTai.setNgayKetThuc(ketThucCu);
        oPhongRepository.saveAndFlush(hienTai);
        OPhong moi = kiemTraVaLuu(req.maSV(), req.soPhongMoi(), req.ngayChuyen(), null);
        return chiTiet(moi);
    }

    private OPhong kiemTraVaLuu(String maSV, String soPhong, LocalDate batDau, LocalDate ketThuc) {
        if (ketThuc != null && !ketThuc.isAfter(batDau)) {
            throw AppException
                    .invalid("Ngày kết thúc phải lớn hơn ngày bắt đầu theo quy ước [NgayBatDau, NgayKetThuc)");
        }
        if (!sinhVienRepository.existsById(maSV)) {
            throw AppException.notFound("Không tìm thấy sinh viên " + maSV);
        }
        Phong phong = phongRepository.findById(soPhong)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy phòng " + soPhong));
        if (oPhongRepository.existsById(new OPhongId(maSV, soPhong, batDau))) {
            throw AppException.exists("Sinh viên đã có bản ghi ở phòng này từ ngày " + batDau);
        }
        LocalDate den = ketThuc != null ? ketThuc : VO_CUC;

        // (1) Một sinh viên không được ở hai phòng chồng thời gian theo nửa khoảng [batDau, den)
        for (OPhong o : oPhongRepository.findByMaSV(maSV)) {
            LocalDate oDen = o.getNgayKetThuc() != null ? o.getNgayKetThuc() : VO_CUC;
            if (batDau.isBefore(oDen) && o.getNgayBatDau().isBefore(den)) {
                throw AppException.rule("Sinh viên " + maSV + " đang ở phòng " + o.getSoPhong()
                        + " trong khoảng thời gian này");
            }
        }

        // (2) Số người trong phòng tại mọi thời điểm không vượt quá SoNguoiToiDa
        List<OPhong> trongPhong = oPhongRepository.findBySoPhong(soPhong);
        Set<LocalDate> moc = new TreeSet<>();
        moc.add(batDau);
        for (OPhong o : trongPhong) {
            if (o.getNgayBatDau().isAfter(batDau) && o.getNgayBatDau().isBefore(den)) {
                moc.add(o.getNgayBatDau());
            }
        }
        for (LocalDate d : moc) {
            long dem = trongPhong.stream().filter(o -> dangO(o, d)).count();
            if (dem >= phong.getSoNguoiToiDa()) {
                throw AppException.rule("Phòng " + soPhong + " đã đủ " + phong.getSoNguoiToiDa()
                        + " người vào ngày " + d);
            }
        }
        return oPhongRepository.saveAndFlush(new OPhong(maSV, soPhong, batDau, ketThuc));
    }

    private static boolean dangO(OPhong o, LocalDate ngay) {
        return !o.getNgayBatDau().isAfter(ngay) && (o.getNgayKetThuc() == null || o.getNgayKetThuc().isAfter(ngay));
    }

    private PhongDto.OPhongResponse chiTiet(OPhong o) {
        return truyVan.chiTietO(o.getMaSV(), o.getSoPhong(), o.getNgayBatDau())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy bản ghi ở phòng"));
    }
}
