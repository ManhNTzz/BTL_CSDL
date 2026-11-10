package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.dto.XeDto;
import com.baitapnhom.quanlyktx.entity.LuotGuiLayXe;
import com.baitapnhom.quanlyktx.entity.LuotGuiLayXeId;
import com.baitapnhom.quanlyktx.entity.VeThangId;
import com.baitapnhom.quanlyktx.exception.AppException;
import com.baitapnhom.quanlyktx.repository.LuotGuiLayXeRepository;
import com.baitapnhom.quanlyktx.repository.TruyVanRepository;
import com.baitapnhom.quanlyktx.repository.VeThangRepository;
import com.baitapnhom.quanlyktx.repository.XeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lượt gửi/lấy xe của xe có vé tháng.
@Service
public class LuotGuiLayXeService {
    @org.springframework.beans.factory.annotation.Autowired
    private WriteGuard writeGuard;

    private final LuotGuiLayXeRepository luotRepository;
    private final VeThangRepository veThangRepository;
    private final XeRepository xeRepository;
    private final TruyVanRepository truyVan;
    private final NamedParameterJdbcTemplate jdbc;

    public LuotGuiLayXeService(LuotGuiLayXeRepository luotRepository, VeThangRepository veThangRepository,
            XeRepository xeRepository, TruyVanRepository truyVan, NamedParameterJdbcTemplate jdbc) {
        this.luotRepository = luotRepository;
        this.veThangRepository = veThangRepository;
        this.xeRepository = xeRepository;
        this.truyVan = truyVan;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<XeDto.LuotResponse> danhSach(String bienSo, LocalDate ngay) {
        return truyVan.luotGuiLay(bienSo, ngay);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public XeDto.LuotResponse ghiNhan(XeDto.LuotRequest req) {
        writeGuard.acquire();
        LocalDateTime gui = req.thoiGianGui() != null ? req.thoiGianGui().withNano(0) : LocalDateTime.now().withNano(0);
        if (req.thoiGianLay() != null && req.thoiGianLay().isBefore(gui)) {
            throw AppException.invalid("Thời gian lấy xe phải lớn hơn hoặc bằng thời gian gửi xe");
        }
        if (!xeRepository.existsById(req.bienSo())) {
            throw AppException.notFound("Không tìm thấy xe " + req.bienSo());
        }
        LocalDate ngayDauThang = gui.toLocalDate().withDayOfMonth(1);
        if (!veThangRepository.existsById(new VeThangId(req.bienSo(), ngayDauThang))) {
            throw AppException.rule("Xe " + req.bienSo() + " chưa đăng ký vé tháng " + YearMonth.from(gui)
                    + " nên không lưu lượt gửi/lấy");
        }

        // Xe đang gửi (chưa lấy ra) hoặc trùng thời gian với lượt khác thì không gửi
        // thêm được
        LocalDateTime lay = req.thoiGianLay() != null ? req.thoiGianLay() : LocalDateTime.MAX;
        for (LuotGuiLayXe l : luotRepository.findByBienSo(req.bienSo())) {
            LocalDateTime lDen = l.getThoiGianLay() != null ? l.getThoiGianLay() : LocalDateTime.MAX;
            if (l.getThoiGianGui().equals(gui) || (l.getThoiGianGui().isBefore(lay) && lDen.isAfter(gui))) {
                throw AppException.rule("Xe " + req.bienSo() + " đang được gửi (chưa lấy ra) hoặc trùng thời gian "
                        + "với lượt gửi lúc " + l.getThoiGianGui());
            }
        }
        if (luotRepository.existsById(new LuotGuiLayXeId(req.bienSo(), ngayDauThang, gui))) {
            throw AppException.exists("Đã có lượt gửi xe vào đúng thời điểm " + gui);
        }

        // Lưu vào bảng LUOT_GUI_LAY_XE
        luotRepository.saveAndFlush(new LuotGuiLayXe(req.bienSo(), ngayDauThang, gui, req.thoiGianLay()));

        // Đọc lại SoTienPhaiTra được tính động từ VIEW V_LUOT_GUI_LAY_XE
        String formattedGui = gui.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        BigDecimal phi = jdbc.getJdbcTemplate().queryForObject(
                "SELECT COALESCE(SoTienPhaiTra, 0) FROM V_LUOT_GUI_LAY_XE WHERE BienSo = ? AND ThoiGianGui = ?",
                BigDecimal.class, req.bienSo(), formattedGui);

        return new XeDto.LuotResponse(req.bienSo(), gui, req.thoiGianLay(), phi != null ? phi : BigDecimal.ZERO);
    }

    // Cập nhật thời gian lấy xe cho một lượt đang gửi
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public XeDto.LuotResponse capLay(XeDto.CapLayRequest req) {
        writeGuard.acquire();
        LocalDateTime gui = req.thoiGianGui().withNano(0);
        LocalDateTime lay = req.thoiGianLay().withNano(0);
        LocalDate thangDangKy = gui.toLocalDate().withDayOfMonth(1);
        if (lay.isBefore(gui)) {
            throw AppException.invalid("Thời gian lấy phải lớn hơn hoặc bằng thời gian gửi");
        }
        LuotGuiLayXe luot = luotRepository.findById(new LuotGuiLayXeId(req.bienSo(), thangDangKy, gui))
                .orElseThrow(() -> AppException.notFound("Không tìm thấy đúng lượt gửi xe này"));
        if (luot.getThoiGianLay() != null) {
            throw AppException.rule("Lượt này đã lấy xe; không ghi đè thời gian lấy");
        }
        for (LuotGuiLayXe other : luotRepository.findByBienSo(req.bienSo())) {
            if (other.getThoiGianGui().equals(gui))
                continue;
            LocalDateTime end = other.getThoiGianLay() == null ? LocalDateTime.MAX : other.getThoiGianLay();
            if (other.getThoiGianGui().isBefore(lay) && end.isAfter(gui)) {
                throw AppException.rule("Thời gian lấy chồng lấn một lượt gửi khác");
            }
        }
        luot.setThoiGianLay(lay);
        luotRepository.saveAndFlush(luot);
        BigDecimal phi = jdbc.getJdbcTemplate().queryForObject(
                "SELECT SoTienPhaiTra FROM V_LUOT_GUI_LAY_XE WHERE BienSo = ? AND ThangDangKy = ? AND ThoiGianGui = ?",
                BigDecimal.class, req.bienSo(), thangDangKy,
                gui.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return new XeDto.LuotResponse(req.bienSo(), gui, lay, phi == null ? BigDecimal.ZERO : phi);
    }

    // Các xe đang gửi trong KTX (chưa lấy ra).
    @Transactional(readOnly = true)
    public List<XeDto.LuotResponse> dangGui() {
        return jdbc.getJdbcTemplate().query("""
                SELECT BienSo, ThoiGianGui, ThoiGianLay, SoTienPhaiTra
                FROM V_LUOT_GUI_LAY_XE
                WHERE ThoiGianLay IS NULL AND ThoiGianGui <= CURRENT_TIMESTAMP
                ORDER BY ThoiGianGui
                """, (rs, i) -> new XeDto.LuotResponse(rs.getString("BienSo"),
                rs.getObject("ThoiGianGui", LocalDateTime.class),
                rs.getObject("ThoiGianLay", LocalDateTime.class),
                rs.getBigDecimal("SoTienPhaiTra")));
    }
}
