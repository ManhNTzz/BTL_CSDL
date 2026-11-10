package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.KhachDto;
import com.baitapnhom.quanlyktx.service.KhachService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Khách đến chơi và các lượt thăm sinh viên.
@RestController
@RequestMapping("/api")
public class KhachController {

    private final KhachService khachService;

    public KhachController(KhachService khachService) {
        this.khachService = khachService;
    }

    @GetMapping("/khach")
    public List<KhachDto.Response> danhSachKhach() {
        return khachService.danhSach();
    }

    // Các lượt khách đến thăm
    @GetMapping("/luot-tham")
    public List<KhachDto.LuotThamResponse> danhSachLuotTham(
            @RequestParam(required = false) String maSV,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return khachService.danhSachLuotTham(maSV, tuNgay, denNgay);
    }

    @PostMapping("/luot-tham")
    @ResponseStatus(HttpStatus.CREATED)
    public KhachDto.LuotThamResponse ghiNhanLuotTham(@Valid @RequestBody KhachDto.LuotThamRequest req) {
        return khachService.ghiNhanLuotTham(req);
    }

    // Hủy một lượt khách đến thăm (khi nhập nhầm).
    @DeleteMapping("/luot-tham")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void huyLuotTham(@RequestParam String cccdKhach, @RequestParam String maSV,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayDenChoi) {
        khachService.huyLuotTham(cccdKhach, maSV, ngayDenChoi);
    }
}
