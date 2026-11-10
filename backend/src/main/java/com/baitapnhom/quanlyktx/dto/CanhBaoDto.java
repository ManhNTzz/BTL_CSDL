package com.baitapnhom.quanlyktx.dto;

import java.time.LocalDate;
import java.util.List;

// Các việc cần chú ý trong ngày: để quản lý KTX xử lý kịp thời.
public final class CanhBaoDto {

    private CanhBaoDto() {
    }

    public record Response(LocalDate ngay, List<XeDto.XeResponse> xeChuaCoVeThangNay,
            List<PhongDto.PhongResponse> phongDayNguoi, List<SinhVienDto.Response> sinhVienChuaCoPhong,
            List<PhongDto.OPhongResponse> sapHetHanO, List<XeDto.LuotResponse> xeGuiTuNgayTruoc) {
    }
}
