package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.XeDto;
import com.baitapnhom.quanlyktx.service.LuotGuiLayXeService;
import com.baitapnhom.quanlyktx.service.VeThangService;
import com.baitapnhom.quanlyktx.service.XeService;
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

// Gửi xe vé tháng: đăng ký xe, vé tháng, ghi nhận lượt gửi/lấy.
@RestController
@RequestMapping("/api")
public class XeController {

    private final XeService xeService;
    private final VeThangService veThangService;
    private final LuotGuiLayXeService luotService;

    public XeController(XeService xeService, VeThangService veThangService, LuotGuiLayXeService luotService) {
        this.xeService = xeService;
        this.veThangService = veThangService;
        this.luotService = luotService;
    }

    @GetMapping("/xe")
    public List<XeDto.XeResponse> danhSachXe(@RequestParam(required = false) String maSV) {
        return xeService.danhSach(maSV);
    }

    @PostMapping("/xe")
    @ResponseStatus(HttpStatus.CREATED)
    public XeDto.XeResponse dangKyXe(@Valid @RequestBody XeDto.XeRequest req) {
        return xeService.dangKy(req);
    }

    @DeleteMapping("/xe/{bienSo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoaXe(@PathVariable String bienSo) {
        xeService.xoa(bienSo);
    }

    @GetMapping("/ve-thang")
    public List<XeDto.VeThangResponse> danhSachVeThang(@RequestParam(required = false) String bienSo) {
        return veThangService.danhSach(bienSo);
    }

    @PostMapping("/ve-thang")
    @ResponseStatus(HttpStatus.CREATED)
    public XeDto.VeThangResponse dangKyVeThang(@Valid @RequestBody XeDto.VeThangRequest req) {
        return veThangService.dangKy(req);
    }

    @GetMapping("/luot-gui-lay-xe")
    public List<XeDto.LuotResponse> danhSachLuot(
            @RequestParam(required = false) String bienSo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        return luotService.danhSach(bienSo, ngay);
    }

    // Ghi nhận lượt gửi xe và số tiền phải trả được tính tự động.
    @PostMapping("/luot-gui-lay-xe")
    @ResponseStatus(HttpStatus.CREATED)
    public XeDto.LuotResponse ghiNhanLuot(@Valid @RequestBody XeDto.LuotRequest req) {
        return luotService.ghiNhan(req);
    }

    @PutMapping("/luot-gui-lay-xe/lay")
    public XeDto.LuotResponse capLayXe(@Valid @RequestBody XeDto.CapLayRequest req) {
        return luotService.capLay(req);
    }

    // Gia hạn vé cho tháng đã cho (yyyy-MM): mọi xe có vé tháng trước sẽ được đăng
    // ký tiếp.
    @PostMapping("/ve-thang/gia-han")
    public XeDto.GiaHanResponse giaHanVeThang(@RequestParam String thang) {
        return veThangService.giaHan(thang);
    }

    // Các xe đang gửi trong KTX (chưa lấy ra).
    @GetMapping("/luot-gui-lay-xe/dang-gui")
    public List<XeDto.LuotResponse> xeDangGui() {
        return luotService.dangGui();
    }
}
