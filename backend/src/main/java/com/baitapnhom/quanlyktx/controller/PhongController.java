package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.PhongDto;
import com.baitapnhom.quanlyktx.service.OPhongService;
import com.baitapnhom.quanlyktx.service.PhongService;
import jakarta.validation.Valid;
import java.time.LocalDate;
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

// Phòng ở và việc sinh viên ở phòng (đăng ký, kết thúc, chuyển phòng).
@RestController
@RequestMapping("/api")
public class PhongController {

    private final PhongService phongService;
    private final OPhongService oPhongService;

    public PhongController(PhongService phongService, OPhongService oPhongService) {
        this.phongService = phongService;
        this.oPhongService = oPhongService;
    }

    // conCho=true: chỉ liệt kê phòng còn chỗ trống vào ngày đã cho (mặc định hôm
    // nay).
    @GetMapping("/phong")
    public List<PhongDto.PhongResponse> danhSachPhong(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(defaultValue = "false") boolean conCho) {
        return phongService.danhSach(ngay, conCho);
    }

    @GetMapping("/phong/{soPhong}")
    public PhongDto.PhongResponse chiTietPhong(@PathVariable String soPhong) {
        return phongService.chiTiet(soPhong);
    }

    @PostMapping("/phong")
    @ResponseStatus(HttpStatus.CREATED)
    public PhongDto.PhongResponse themPhong(@Valid @RequestBody PhongDto.PhongRequest req) {
        return phongService.them(req);
    }

    @PutMapping("/phong/{soPhong}")
    public PhongDto.PhongResponse capNhatPhong(@PathVariable String soPhong,
            @Valid @RequestBody PhongDto.PhongRequest req) {
        return phongService.capNhat(soPhong, req);
    }

    @DeleteMapping("/phong/{soPhong}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoaPhong(@PathVariable String soPhong) {
        phongService.xoa(soPhong);
    }

    // Sinh viên đang ở trong phòng vào một ngày.
    @GetMapping("/phong/{soPhong}/sinh-vien")
    public List<PhongDto.OPhongResponse> sinhVienTrongPhong(@PathVariable String soPhong,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        return oPhongService.dangO(soPhong, ngay);
    }

    // Lịch sử ở phòng của một sinh viên.
    @GetMapping("/o-phong")
    public List<PhongDto.OPhongResponse> lichSuOPhong(@RequestParam String maSV) {
        return oPhongService.lichSu(maSV);
    }

    @PostMapping("/o-phong")
    @ResponseStatus(HttpStatus.CREATED)
    public PhongDto.OPhongResponse dangKyOPhong(@Valid @RequestBody PhongDto.OPhongRequest req) {
        return oPhongService.dangKy(req);
    }

    @PutMapping("/o-phong/ket-thuc")
    public PhongDto.OPhongResponse ketThucOPhong(@Valid @RequestBody PhongDto.KetThucRequest req) {
        return oPhongService.ketThuc(req);
    }

    @PostMapping("/o-phong/chuyen")
    public PhongDto.OPhongResponse chuyenPhong(@Valid @RequestBody PhongDto.ChuyenPhongRequest req) {
        return oPhongService.chuyenPhong(req);
    }
}
