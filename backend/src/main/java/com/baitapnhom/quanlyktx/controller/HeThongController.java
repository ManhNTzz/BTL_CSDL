package com.baitapnhom.quanlyktx.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/he-thong")
public class HeThongController {
    @GetMapping("/trang-thai") public Map<String,String> trangThai() {
        return Map.of("ungDung","QuanLyKTX", "phienBan","1.1.0");
    }
}
