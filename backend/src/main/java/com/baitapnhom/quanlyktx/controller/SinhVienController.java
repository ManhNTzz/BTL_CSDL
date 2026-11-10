package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.SinhVienDto;
import com.baitapnhom.quanlyktx.service.HoSoService;
import com.baitapnhom.quanlyktx.service.SinhVienService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sinh-vien")
public class SinhVienController {

    private final SinhVienService service;
    private final HoSoService hoSoService;

    public SinhVienController(SinhVienService service, HoSoService hoSoService) {
        this.service = service;
        this.hoSoService = hoSoService;
    }

    // Tìm kiếm theo mã SV, họ tên, lớp, quê quán, bỏ trống tuKhoa = tất cả.
    @GetMapping
    public List<SinhVienDto.Response> danhSach(@RequestParam(required = false) String tuKhoa) {
        return service.timKiem(tuKhoa);
    }

    @GetMapping("/{maSV}")
    public SinhVienDto.Response chiTiet(@PathVariable String maSV) {
        return service.chiTiet(maSV);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SinhVienDto.Response them(@Valid @RequestBody SinhVienDto.Request req) {
        return service.them(req);
    }

    @PutMapping("/{maSV}")
    public SinhVienDto.Response capNhat(@PathVariable String maSV,
            @Valid @RequestBody SinhVienDto.UpdateRequest req) {
        return service.capNhat(maSV, req);
    }

    @DeleteMapping("/{maSV}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoa(@PathVariable String maSV) {
        service.xoa(maSV);
    }

    // Hồ sơ đầy đủ: phòng, xe, dịch vụ đã dùng, khách đến thăm và hóa đơn các
    // tháng.
    @GetMapping("/{maSV}/ho-so")
    public SinhVienDto.HoSo hoSo(@PathVariable String maSV) {
        return hoSoService.hoSo(maSV);
    }
}
