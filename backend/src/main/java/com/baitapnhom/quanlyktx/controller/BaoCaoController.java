package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.BaoCaoDto;
import com.baitapnhom.quanlyktx.service.BaoCaoService;
import com.baitapnhom.quanlyktx.service.ExportService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 4 truy vấn của đề bài + thống kê tổng quan
@RestController
@RequestMapping("/api")
public class BaoCaoController {

    private final BaoCaoService service;
    private final ExportService exportService;

    public BaoCaoController(BaoCaoService service, ExportService exportService) {
        this.service = service;
        this.exportService = exportService;
    }

    // Truy vấn 1
    @GetMapping("/bao-cao/hoa-don")
    public List<BaoCaoDto.HoaDonThang> hoaDon(@RequestParam(name = "thang", required = false) String thang,
            @RequestParam(name = "maSV", required = false) String maSV) {
        return service.hoaDon(thang, maSV);
    }

    // Truy vấn 2
    @GetMapping("/bao-cao/dich-vu-sinh-vien")
    public List<BaoCaoDto.DichVuSinhVien> dichVuSinhVien(
            @RequestParam(name = "tuNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.dichVuSinhVien(tuNgay, denNgay);
    }

    // Truy vấn 3
    @GetMapping("/bao-cao/khach-tham")
    public List<BaoCaoDto.KhachTham> khachTham(
            @RequestParam(name = "kieu", required = false) String kieu,
            @RequestParam(name = "ngay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(name = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.khachTham(kieu, ngay, tuNgay, denNgay);
    }

    // Truy vấn 4
    @GetMapping("/bao-cao/doanh-thu-dich-vu")
    public List<BaoCaoDto.DoanhThuDichVu> doanhThuDichVu(@RequestParam(name = "thang", required = false) String thang) {
        return service.doanhThuDichVu(thang);
    }

    @GetMapping("/thong-ke/tong-quan")
    public BaoCaoDto.TongQuan tongQuan() {
        return service.tongQuan();
    }

    // Tổng doanh thu mỗi tháng KTX(dùng để vẽ biểu đồ)
    @GetMapping("/thong-ke/doanh-thu-theo-thang")
    public List<BaoCaoDto.DoanhThuThang> doanhThuTheoThang() {
        return service.doanhThuTheoThang();
    }

    // Xuất file Excel

    @GetMapping("/bao-cao/hoa-don/csv")
    public ResponseEntity<byte[]> hoaDonCsv(@RequestParam(name = "thang", required = false) String thang,
            @RequestParam(name = "maSV", required = false) String maSV) {
        return csv("hoa-don.csv", exportService.hoaDon(thang, maSV));
    }

    @GetMapping("/bao-cao/dich-vu-sinh-vien/csv")
    public ResponseEntity<byte[]> dichVuSinhVienCsv(
            @RequestParam(name = "tuNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return csv("dich-vu-sinh-vien.csv", exportService.dichVuSinhVien(tuNgay, denNgay));
    }

    @GetMapping("/bao-cao/khach-tham/csv")
    public ResponseEntity<byte[]> khachThamCsv(
            @RequestParam(name = "kieu", required = false) String kieu,
            @RequestParam(name = "ngay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(name = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return csv("khach-tham.csv", exportService.khachTham(kieu, ngay, tuNgay, denNgay));
    }

    @GetMapping("/bao-cao/doanh-thu-dich-vu/csv")
    public ResponseEntity<byte[]> doanhThuDichVuCsv(@RequestParam(name = "thang", required = false) String thang) {
        return csv("doanh-thu-dich-vu.csv", exportService.doanhThuDichVu(thang));
    }

    private static ResponseEntity<byte[]> csv(String tenFile, byte[] noiDung) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + tenFile + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(noiDung);
    }
}
