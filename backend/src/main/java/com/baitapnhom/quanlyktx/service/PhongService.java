package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.entity.Phong;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.PhongRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhongService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;

    private final PhongRepository phongRepository;
    private final TruyVanRepository truyVan;

    public PhongService(PhongRepository phongRepository, TruyVanRepository truyVan) {
        this.phongRepository = phongRepository;
        this.truyVan = truyVan;
    }
    
    // Danh sách phòng kèm số người đang ở vào ngày đã cho (mặc định hôm nay).
    @Transactional(readOnly = true)
    public List<PhongDto.PhongResponse> danhSach(LocalDate ngay, boolean chiPhongConCho) {
        LocalDate d = ngay != null ? ngay : LocalDate.now();
        return truyVan.phongs(d).stream().filter(p -> !chiPhongConCho || p.conCho() > 0).toList();
    }

    @Transactional(readOnly = true)
    public PhongDto.PhongResponse chiTiet(String soPhong) {
        return truyVan.phong(soPhong, LocalDate.now())
                .orElseThrow(() -> AppException.notFound("Không tìm thấy phòng " + soPhong));
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public PhongDto.PhongResponse them(PhongDto.PhongRequest req) {
        writeGuard.acquire();
        String soPhong = req.soPhong().trim();
        if (phongRepository.existsById(soPhong)) {
            throw AppException.exists("Phòng " + soPhong + " đã tồn tại");
        }
        phongRepository.saveAndFlush(new Phong(soPhong, req.loaiPhong().trim(), req.donGia(), req.soNguoiToiDa()));
        return chiTiet(soPhong);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public PhongDto.PhongResponse capNhat(String soPhong, PhongDto.PhongRequest req) {
        writeGuard.acquire();
        Phong phong = phongRepository.findById(soPhong)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy phòng " + soPhong));
        int dangO = chiTiet(soPhong).soNguoiDangO();
        if (req.soNguoiToiDa() < dangO) {
            throw AppException.rule("Số người tối đa không được nhỏ hơn số người đang ở (" + dangO + ")");
        }
        phong.setLoaiPhong(req.loaiPhong().trim());
        phong.setDonGia(req.donGia());
        phong.setSoNguoiToiDa(req.soNguoiToiDa());
        phongRepository.saveAndFlush(phong);
        return chiTiet(soPhong);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void xoa(String soPhong) {
        writeGuard.acquire();
        if (!phongRepository.existsById(soPhong)) {
            throw AppException.notFound("Không tìm thấy phòng " + soPhong);
        }
        try {
            phongRepository.deleteById(soPhong);
            phongRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw AppException.rule("Không thể xóa phòng " + soPhong + " vì đã có sinh viên ở trong lịch sử");
        }
    }
}
