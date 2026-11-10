package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.DichVuDto;
import com.baitapnhom.quanlyktx.entity.DichVu;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.DichVuRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DichVuService {
    @org.springframework.beans.factory.annotation.Autowired private WriteGuard writeGuard;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private final DichVuRepository dichVuRepository;

    public DichVuService(DichVuRepository dichVuRepository) {
        this.dichVuRepository = dichVuRepository;
    }

    @Transactional(readOnly = true)
    public List<DichVuDto.Response> danhSach() {
        return dichVuRepository.findAll().stream()
                .sorted(Comparator.comparing(DichVu::getMaDV))
                .map(DichVuService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DichVuDto.Response chiTiet(String maDV) {
        return toResponse(tim(maDV));
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public DichVuDto.Response them(DichVuDto.Request req) {
        writeGuard.acquire();
        String maDV = req.maDV().trim();
        if (dichVuRepository.existsById(maDV)) {
            throw AppException.exists("Mã dịch vụ " + maDV + " đã tồn tại");
        }
        return toResponse(dichVuRepository.saveAndFlush(new DichVu(maDV, req.tenDV().trim(), req.donGia())));
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public DichVuDto.Response capNhat(String maDV, DichVuDto.Request req) {
        writeGuard.acquire();
        DichVu dv = tim(maDV);
        if ("Trông xe".equalsIgnoreCase(dv.getTenDV()) && !"Trông xe".equalsIgnoreCase(req.tenDV().trim())) {
            throw AppException.rule("Giữ tên Trông xe vì các báo cáo dùng tên này để nhận diện tiền xe");
        }
        dv.setTenDV(req.tenDV().trim());
        dv.setDonGia(req.donGia());
        return toResponse(dichVuRepository.saveAndFlush(dv));
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void xoa(String maDV) {
        writeGuard.acquire();
        DichVu dv = tim(maDV);
        if ("Trông xe".equalsIgnoreCase(dv.getTenDV())) throw AppException.rule("Không xóa danh mục Trông xe");
        if (jdbc.queryForObject("SELECT COUNT(*) FROM LAN_SU_DUNG WHERE MaDV=?", Long.class, maDV) > 0)
            throw AppException.rule("Dịch vụ đã được sử dụng, không xóa dữ liệu lịch sử");
        try {
            dichVuRepository.deleteById(maDV);
            dichVuRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw AppException.rule("Không thể xóa dịch vụ " + maDV + " vì đã có sinh viên sử dụng");
        }
    }

    private DichVu tim(String maDV) {
        return dichVuRepository.findById(maDV)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy dịch vụ " + maDV));
    }

    private static DichVuDto.Response toResponse(DichVu dv) {
        return new DichVuDto.Response(dv.getMaDV(), dv.getTenDV(), dv.getDonGia());
    }
}
