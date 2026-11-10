package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.DichVuDto;
import com.baitapnhom.quanlyktx.service.DichVuService;
import com.baitapnhom.quanlyktx.service.SuDungDichVuService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
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

// Danh mục dịch vụ và việc ghi nhận sinh viên sử dụng dịch vụ.
@RestController
@RequestMapping("/api")
public class DichVuController {

    private final DichVuService dichVuService;
    private final SuDungDichVuService suDungService;

    public DichVuController(DichVuService dichVuService, SuDungDichVuService suDungService) {
        this.dichVuService = dichVuService;
        this.suDungService = suDungService;
    }

    @GetMapping("/dich-vu")
    public List<DichVuDto.Response> danhSach() {
        return dichVuService.danhSach();
    }

    @GetMapping("/dich-vu/{maDV}")
    public DichVuDto.Response chiTiet(@PathVariable String maDV) {
        return dichVuService.chiTiet(maDV);
    }

    @PostMapping("/dich-vu")
    @ResponseStatus(HttpStatus.CREATED)
    public DichVuDto.Response them(@Valid @RequestBody DichVuDto.Request req) {
        return dichVuService.them(req);
    }

    @PutMapping("/dich-vu/{maDV}")
    public DichVuDto.Response capNhat(@PathVariable String maDV, @Valid @RequestBody DichVuDto.Request req) {
        return dichVuService.capNhat(maDV, req);
    }

    @DeleteMapping("/dich-vu/{maDV}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoa(@PathVariable String maDV) {
        dichVuService.xoa(maDV);
    }

    // Các lần sử dụng dịch vụ
    @GetMapping("/su-dung-dich-vu")
    public List<DichVuDto.SuDungResponse> danhSachSuDung(
            @RequestParam(required = false) String maSV,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return suDungService.danhSach(maSV, tuNgay, denNgay);
    }

    @PostMapping("/su-dung-dich-vu")
    @ResponseStatus(HttpStatus.CREATED)
    public DichVuDto.SuDungResponse ghiNhanSuDung(@Valid @RequestBody DichVuDto.SuDungRequest req) {
        return suDungService.ghiNhan(req);
    }

    // Hủy một lần ghi nhận sử dụng dịch vụ (khi nhập nhầm)
    @DeleteMapping("/su-dung-dich-vu")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void huySuDung(@RequestParam String maSV, @RequestParam String maDV,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime thoiGianSD) {
        suDungService.huy(maSV, maDV, thoiGianSD);
    }
}
