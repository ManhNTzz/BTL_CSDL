package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.BaoCaoDto;
import com.baitapnhom.quanlyktx.util.CsvUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

// Xuất kết quả truy vấn ra file (mở bằng Excel)
@Service
public class ExportService {

    private final BaoCaoService baoCaoService;

    public ExportService(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    public byte[] hoaDon(String thang, String maSV) {
        List<List<Object>> dong = new ArrayList<>();
        for (BaoCaoDto.HoaDonThang h : baoCaoService.hoaDon(thang, maSV)) {
            dong.add(Arrays.<Object>asList(h.maSV(), h.cccd(), h.hoTen(), h.ngaySinh(), h.lop(), h.queQuan(),
                    h.thang(), h.phongTinhTien(), h.tienPhong(), h.tienDichVu(), h.tienVeThang(), h.tienPhatSinhXe(), h.tongTien()));
        }
        return CsvUtil.build(List.of("Mã SV", "Số CCCD", "Họ và tên", "Ngày sinh", "Lớp", "Quê quán", "Tháng",
                "Phòng tính tiền", "Tiền phòng", "Tiền dịch vụ", "Tiền gửi xe vé tháng", "Tiền phát sinh xe", "Tổng tiền phải trả"),
                dong);
    }

    public byte[] dichVuSinhVien(LocalDate tuNgay, LocalDate denNgay) {
        List<List<Object>> dong = new ArrayList<>();
        for (BaoCaoDto.DichVuSinhVien d : baoCaoService.dichVuSinhVien(tuNgay, denNgay)) {
            dong.add(Arrays.<Object>asList(d.maSV(), d.cccd(), d.hoTen(), d.ngaySinh(), d.lop(), d.queQuan(),
                    d.tenDV(), d.tongGiaDV()));
        }
        return CsvUtil.build(List.of("Mã SV", "Số CCCD", "Họ và tên", "Ngày sinh", "Lớp", "Quê quán",
                "Tên dịch vụ", "Tổng giá dịch vụ"), dong);
    }

    public byte[] khachTham(String kieu, LocalDate ngay, LocalDate tuNgay, LocalDate denNgay) {
        List<List<Object>> dong = new ArrayList<>();
        for (BaoCaoDto.KhachTham k : baoCaoService.khachTham(kieu, ngay, tuNgay, denNgay)) {
            dong.add(Arrays.<Object>asList(k.maSV(), k.cccdSV(), k.hoTenSV(), k.ngaySinhSV(), k.lop(), k.queQuan(),
                    k.cccdKhach(), k.hoTenKhach(), k.ngaySinhKhach(), k.soLanDenChoi(),
                    String.join("; ", k.cacLanDen())));
        }
        return CsvUtil.build(List.of("Mã SV", "CCCD sinh viên", "Họ tên sinh viên", "Ngày sinh SV", "Lớp",
                "Quê quán", "CCCD khách", "Họ tên khách", "Ngày sinh khách", "Số lần đến chơi", "Các lần đến"),
                dong);
    }

    public byte[] doanhThuDichVu(String thang) {
        List<List<Object>> dong = new ArrayList<>();
        for (BaoCaoDto.DoanhThuDichVu d : baoCaoService.doanhThuDichVu(thang)) {
            dong.add(Arrays.<Object>asList(d.maDV(), d.tenDV(), "Trông xe".equalsIgnoreCase(d.tenDV()) ? "100.000 đ/vé tháng, 3.000 đ/lượt phát sinh" : d.donGia(), d.thang(), d.soLuong(),
                    d.doanhThuDichVu(), d.doanhThuVeThang(), d.doanhThuPhatSinh(), d.tongDoanhThu()));
        }
        return CsvUtil.build(List.of("Mã DV", "Tên dịch vụ", "Đơn giá", "Tháng", "Số lượng", "Doanh thu dịch vụ",
                "Doanh thu vé tháng", "Doanh thu gửi/lấy xe phát sinh", "Tổng doanh thu"), dong);
    }
}
