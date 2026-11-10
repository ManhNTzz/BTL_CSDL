package com.baitapnhom.quanlyktx.controller;

import com.baitapnhom.quanlyktx.dto.CanhBaoDto;
import com.baitapnhom.quanlyktx.service.ThongKeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ThongKeController {

    private final ThongKeService service;

    public ThongKeController(ThongKeService service) {
        this.service = service;
    }

    // Cảnh báo và nhắc việc trong ngày cho quản lý KTX.
    @GetMapping("/canh-bao")
    public CanhBaoDto.Response canhBao() {
        return service.canhBao();
    }
}
