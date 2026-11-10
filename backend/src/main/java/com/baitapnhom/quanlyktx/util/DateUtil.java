package com.baitapnhom.quanlyktx.util;

import com.baitapnhom.quanlyktx.exception.AppException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

public final class DateUtil {

    private DateUtil() {
    }

    // Đọc tháng dạng yyyy-MM, ví dụ 2026-10.
    public static YearMonth parseThang(String thang) {
        try {
            return YearMonth.parse(thang.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            throw AppException.invalid("Tháng phải có dạng yyyy-MM, ví dụ 2026-10");
        }
    }

    public static void kiemTraKhoang(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null) {
            throw AppException.invalid("Cần nhập đủ từ ngày và đến ngày");
        }
        if (denNgay.isBefore(tuNgay)) {
            throw AppException.invalid("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
    }

    // Thứ Hai đến Chủ nhật của tuần chứa ngày đã cho.
    public static LocalDate[] khoangTuan(LocalDate ngay) {
        LocalDate dau = ngay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new LocalDate[] {dau, dau.plusDays(6)};
    }

    // Ngày đầu đến ngày cuối của tháng chứa ngày đã cho.
    public static LocalDate[] khoangThang(LocalDate ngay) {
        YearMonth ym = YearMonth.from(ngay);
        return new LocalDate[] {ym.atDay(1), ym.atEndOfMonth()};
    }
}
