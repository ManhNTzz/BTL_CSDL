SET NAMES utf8mb4;
CREATE DATABASE QuanLyKTX CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QuanLyKTX;

-- Thực thể cha NGUOI (người)
CREATE TABLE NGUOI (
    CCCD     CHAR(12)     NOT NULL,
    HoTen    VARCHAR(100) NOT NULL,
    NgaySinh DATE         NOT NULL,
    PRIMARY KEY (CCCD)
) ENGINE = InnoDB;

-- Thực thể con SINH_VIEN (sinh viên)
CREATE TABLE SINH_VIEN (
    MaSV    VARCHAR(10)  NOT NULL,
    CCCD    CHAR(12)     NOT NULL,
    Lop     VARCHAR(20)  NOT NULL,
    QueQuan VARCHAR(100) NOT NULL,
    PRIMARY KEY (MaSV),
    UNIQUE KEY uq_sv_cccd (CCCD),
    CONSTRAINT fk_sv_nguoi FOREIGN KEY (CCCD) REFERENCES NGUOI (CCCD)
) ENGINE = InnoDB;

-- Thực thể con KHACH (khách)
CREATE TABLE KHACH (
    CCCD CHAR(12) NOT NULL,
    PRIMARY KEY (CCCD),
    CONSTRAINT fk_khach_nguoi FOREIGN KEY (CCCD) REFERENCES NGUOI (CCCD)
) ENGINE = InnoDB;

-- Thực thể PHONG (phòng)
CREATE TABLE PHONG (
    SoPhong      VARCHAR(10)      NOT NULL,
    LoaiPhong    VARCHAR(30)      NOT NULL,
    DonGia       DECIMAL(12, 0)   NOT NULL,
    SoNguoiToiDa TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (SoPhong),
    CONSTRAINT ck_phong_dongia  CHECK (DonGia >= 0),
    CONSTRAINT ck_phong_songuoi CHECK (SoNguoiToiDa > 0)
) ENGINE = InnoDB;

-- Thực thể yếu O_PHONG (ở phòng)
CREATE TABLE O_PHONG (
    MaSV        VARCHAR(10) NOT NULL,
    SoPhong     VARCHAR(10) NOT NULL,
    NgayBatDau  DATE        NOT NULL,
    NgayKetThuc DATE        NULL, -- NULL = chưa xác định ngày kết thúc
    PRIMARY KEY (MaSV, SoPhong, NgayBatDau),
    CONSTRAINT fk_op_sv    FOREIGN KEY (MaSV)    REFERENCES SINH_VIEN (MaSV),
    CONSTRAINT fk_op_phong FOREIGN KEY (SoPhong) REFERENCES PHONG (SoPhong),
    CONSTRAINT ck_op_ngay  CHECK (NgayKetThuc IS NULL OR NgayKetThuc > NgayBatDau)
) ENGINE = InnoDB;

-- Thực thể yếu LUOT_THAM (lượt thăm)
CREATE TABLE LUOT_THAM (
    CCCD_Khach  CHAR(12)    NOT NULL,
    MaSV        VARCHAR(10) NOT NULL,
    NgayDenChoi DATETIME    NOT NULL,
    PRIMARY KEY (CCCD_Khach, MaSV, NgayDenChoi),
    CONSTRAINT fk_lt_khach FOREIGN KEY (CCCD_Khach) REFERENCES KHACH (CCCD)     ON DELETE CASCADE,
    CONSTRAINT fk_lt_sv    FOREIGN KEY (MaSV)       REFERENCES SINH_VIEN (MaSV) ON DELETE CASCADE
) ENGINE = InnoDB;

-- Thực thể DICH_VU (dịch vụ)
CREATE TABLE DICH_VU (
    MaDV   VARCHAR(10)    NOT NULL,
    TenDV  VARCHAR(50)    NOT NULL,
    DonGia DECIMAL(12, 0) NOT NULL,
    PRIMARY KEY (MaDV),
    UNIQUE KEY uq_dv_ten (TenDV),
    CONSTRAINT ck_dv_dongia CHECK (DonGia >= 0)
) ENGINE = InnoDB;

-- Thực thể yếu LAN_SU_DUNG (lần sử dụng)
CREATE TABLE LAN_SU_DUNG (
    MaSV       VARCHAR(10) NOT NULL,
    MaDV       VARCHAR(10) NOT NULL,
    ThoiGianSD DATETIME    NOT NULL,
    SoLuong    INT         NOT NULL DEFAULT 1,
    PRIMARY KEY (MaSV, MaDV, ThoiGianSD),
    CONSTRAINT fk_lsd_sv      FOREIGN KEY (MaSV) REFERENCES SINH_VIEN (MaSV) ON DELETE CASCADE,
    CONSTRAINT fk_lsd_dv      FOREIGN KEY (MaDV) REFERENCES DICH_VU (MaDV)   ON DELETE CASCADE,
    CONSTRAINT ck_lsd_soluong CHECK (SoLuong > 0)
) ENGINE = InnoDB;

-- Thực thể XE (xe)
CREATE TABLE XE (
    BienSo VARCHAR(15) NOT NULL,
    LoaiXe VARCHAR(30) NOT NULL,
    MaSV   VARCHAR(10) NOT NULL,
    PRIMARY KEY (BienSo),
    CONSTRAINT fk_xe_sv FOREIGN KEY (MaSV) REFERENCES SINH_VIEN (MaSV)
) ENGINE = InnoDB;

-- Thực thể yếu VE_THANG (vé tháng)
CREATE TABLE VE_THANG (
    BienSo      VARCHAR(15)    NOT NULL,
    ThangDangKy DATE           NOT NULL,
    DonGiaVe    DECIMAL(10, 0) NOT NULL DEFAULT 100000,
    PRIMARY KEY (BienSo, ThangDangKy),
    CONSTRAINT fk_vt_xe     FOREIGN KEY (BienSo) REFERENCES XE (BienSo) ON DELETE CASCADE,
    CONSTRAINT ck_vt_thang  CHECK (DAY(ThangDangKy) = 1),
    CONSTRAINT ck_vt_dongia CHECK (DonGiaVe = 100000)
) ENGINE = InnoDB;

-- Thực thể yếu LUOT_GUI_LAY_XE (lượt gửi lấy xe)
CREATE TABLE LUOT_GUI_LAY_XE (
    BienSo        VARCHAR(15)    NOT NULL,
    ThangDangKy   DATE           NOT NULL,
    ThoiGianGui   DATETIME       NOT NULL,
    ThoiGianLay   DATETIME       NULL,               -- NULL = lượt gửi chưa kết thúc
    PRIMARY KEY (BienSo, ThangDangKy, ThoiGianGui),
    CONSTRAINT fk_luot_vethang FOREIGN KEY (BienSo, ThangDangKy)
        REFERENCES VE_THANG (BienSo, ThangDangKy) ON DELETE CASCADE,
    CONSTRAINT ck_luot_tg  CHECK (ThoiGianLay IS NULL OR ThoiGianLay >= ThoiGianGui)
) ENGINE = InnoDB;

-- SoTienPhaiTra là thuộc tính dẫn xuất, tính động theo ngày gửi và thứ tự gửi.
-- SELECT * FROM V_LUOT_GUI_LAY_XE để xem cả thông tin lượt và phí.
CREATE VIEW V_LUOT_GUI_LAY_XE AS
SELECT l.BienSo, l.ThangDangKy, l.ThoiGianGui, l.ThoiGianLay,
       CASE WHEN ROW_NUMBER() OVER (PARTITION BY l.BienSo, DATE(l.ThoiGianGui)
                                    ORDER BY l.ThoiGianGui) <= 2
            THEN 0 ELSE 3000 END AS SoTienPhaiTra
FROM LUOT_GUI_LAY_XE l;

-- các thủ tục và trigger kiểm tra ràng buộc
DELIMITER $$

-- Một NGUOI không thể vừa là SINH_VIEN vừa là KHACH
CREATE TRIGGER trg_sv_bi BEFORE INSERT ON SINH_VIEN
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM KHACH WHERE CCCD = NEW.CCCD) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Người này đã là khách, không thể đồng thời là sinh viên (ISA disjoint)';
    END IF;
END$$

CREATE TRIGGER trg_sv_bu BEFORE UPDATE ON SINH_VIEN
FOR EACH ROW
BEGIN
    IF NEW.CCCD <> OLD.CCCD AND EXISTS (SELECT 1 FROM KHACH WHERE CCCD = NEW.CCCD) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Người này đã là khách, không thể đồng thời là sinh viên';
    END IF;
END$$

CREATE TRIGGER trg_khach_bi BEFORE INSERT ON KHACH
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM SINH_VIEN WHERE CCCD = NEW.CCCD) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Người này đã là sinh viên, không thể đồng thời là khách';
    END IF;
END$$

CREATE TRIGGER trg_khach_bu BEFORE UPDATE ON KHACH
FOR EACH ROW
BEGIN
    IF NEW.CCCD <> OLD.CCCD AND EXISTS (SELECT 1 FROM SINH_VIEN WHERE CCCD = NEW.CCCD) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Người này đã là sinh viên, không thể đồng thời là khách';
    END IF;
END$$

-- Ràng buộc O_PHONG theo nửa khoảng [NgayBatDau, NgayKetThuc):
-- (a) Các khoảng lưu trú của cùng 1 sinh viên không chồng lấn (chuyển phòng ngày D: đợt cũ kết thúc ngày D, đợt mới bắt đầu ngày D là hợp lệ)
-- (b) Tại mọi ngày d, số sinh viên có đợt ở hiệu lực (NgayBatDau <= d < NgayKetThuc) không vượt quá SoNguoiToiDa
CREATE PROCEDURE sp_kiem_tra_o_phong(
    IN p_masv    VARCHAR(10),
    IN p_sophong VARCHAR(10),
    IN p_bd      DATE,
    IN p_kt      DATE,
    IN o_masv    VARCHAR(10),
    IN o_sophong VARCHAR(10),
    IN o_bd      DATE
)
BEGIN
    DECLARE v_max INT DEFAULT NULL;
    DECLARE v_cnt INT DEFAULT 0;
    DECLARE v_kt  DATE;

    IF p_kt IS NOT NULL AND p_kt <= p_bd THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'NgayKetThuc phải lớn hơn NgayBatDau theo quy ước [NgayBatDau, NgayKetThuc)';
    END IF;

    SET v_kt = COALESCE(p_kt, '9999-12-31');

    -- Kiểm tra chồng lấn nửa khoảng [p_bd, v_kt) với các đợt khác của cùng sinh viên
    SELECT COUNT(*) INTO v_cnt
    FROM O_PHONG o
    WHERE o.MaSV = p_masv
      AND o.NgayBatDau < v_kt
      AND COALESCE(o.NgayKetThuc, '9999-12-31') > p_bd
      AND NOT (o.MaSV <=> o_masv AND o.SoPhong <=> o_sophong AND o.NgayBatDau <=> o_bd);

    IF v_cnt > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Khoảng thời gian lưu trú của sinh viên bị chồng lấn với một đợt ở khác';
    END IF;

    -- Kiểm tra sức chứa tối đa của phòng tại mọi mốc bắt đầu đợt ở trong khoảng [p_bd, v_kt)
    SELECT SoNguoiToiDa INTO v_max FROM PHONG WHERE SoPhong = p_sophong;
    IF v_max IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phòng không tồn tại';
    END IF;

    SELECT COALESCE(MAX(x.c), 0) INTO v_cnt
    FROM (
        SELECT t.d,
               (SELECT COUNT(*)
                  FROM O_PHONG o
                 WHERE o.SoPhong = p_sophong
                   AND o.NgayBatDau <= t.d
                   AND COALESCE(o.NgayKetThuc, '9999-12-31') > t.d
                   AND NOT (o.MaSV <=> o_masv AND o.SoPhong <=> o_sophong AND o.NgayBatDau <=> o_bd)) AS c
        FROM (
            SELECT p_bd AS d
            UNION
            SELECT o2.NgayBatDau
              FROM O_PHONG o2
             WHERE o2.SoPhong = p_sophong
               AND o2.NgayBatDau > p_bd
               AND o2.NgayBatDau < v_kt
               AND NOT (o2.MaSV <=> o_masv AND o2.SoPhong <=> o_sophong AND o2.NgayBatDau <=> o_bd)
        ) t
    ) x;

    IF v_cnt >= v_max THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Phòng đã đạt số người ở tối đa (SoNguoiToiDa) trong khoảng thời gian này';
    END IF;
END$$

CREATE TRIGGER trg_o_phong_bi BEFORE INSERT ON O_PHONG
FOR EACH ROW
BEGIN
    CALL sp_kiem_tra_o_phong(NEW.MaSV, NEW.SoPhong, NEW.NgayBatDau, NEW.NgayKetThuc, NULL, NULL, NULL);
END$$

CREATE TRIGGER trg_o_phong_bu BEFORE UPDATE ON O_PHONG
FOR EACH ROW
BEGIN
    CALL sp_kiem_tra_o_phong(NEW.MaSV, NEW.SoPhong, NEW.NgayBatDau, NEW.NgayKetThuc, OLD.MaSV, OLD.SoPhong, OLD.NgayBatDau);
END$$

-- Kiểm tra khi giảm SoNguoiToiDa của PHONG
CREATE TRIGGER trg_phong_bu BEFORE UPDATE ON PHONG
FOR EACH ROW
BEGIN
    DECLARE v_max_dang_o INT DEFAULT 0;
    IF NEW.SoNguoiToiDa < OLD.SoNguoiToiDa THEN
        SELECT COALESCE(MAX(sub.cnt), 0) INTO v_max_dang_o
        FROM (
            SELECT (SELECT COUNT(*)
                      FROM O_PHONG o2
                     WHERE o2.SoPhong = NEW.SoPhong
                       AND o2.NgayBatDau <= o1.NgayBatDau
                       AND COALESCE(o2.NgayKetThuc, '9999-12-31') > o1.NgayBatDau) AS cnt
            FROM O_PHONG o1
            WHERE o1.SoPhong = NEW.SoPhong
        ) sub;

        IF v_max_dang_o > NEW.SoNguoiToiDa THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Không thể giảm SoNguoiToiDa xuống thấp hơn số sinh viên đã/đang ở cùng lúc trong phòng';
        END IF;
    END IF;
END$$

-- Ràng buộc giới hạn mỗi sinh viên đăng ký tối đa 2 xe vé tháng 
CREATE TRIGGER trg_xe_bi BEFORE INSERT ON XE
FOR EACH ROW
BEGIN
    IF (SELECT COUNT(*) FROM XE WHERE MaSV = NEW.MaSV) >= 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Mỗi sinh viên chỉ được phép đăng ký gửi tối đa 2 xe vé tháng';
    END IF;
END$$

CREATE TRIGGER trg_xe_bu BEFORE UPDATE ON XE
FOR EACH ROW
BEGIN
    IF NEW.MaSV <> OLD.MaSV
       AND (SELECT COUNT(*) FROM XE WHERE MaSV = NEW.MaSV) >= 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Mỗi sinh viên chỉ được phép đăng ký gửi tối đa 2 xe vé tháng';
    END IF;
END$$

-- Ràng buộc không ghi dịch vụ 'Trông xe' vào LAN_SU_DUNG để tránh tính trùng tiền
CREATE TRIGGER trg_lsd_bi BEFORE INSERT ON LAN_SU_DUNG
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM DICH_VU WHERE MaDV = NEW.MaDV AND TenDV = 'Trông xe') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Dịch vụ Trông xe của sinh viên được quản lý qua VE_THANG và LUOT_GUI_LAY_XE, không ghi vào LAN_SU_DUNG';
    END IF;
END$$

CREATE TRIGGER trg_lsd_bu BEFORE UPDATE ON LAN_SU_DUNG
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM DICH_VU WHERE MaDV = NEW.MaDV AND TenDV = 'Trông xe') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Dịch vụ Trông xe của sinh viên được quản lý qua VE_THANG và LUOT_GUI_LAY_XE, không ghi vào LAN_SU_DUNG';
    END IF;
END$$

-- Mỗi lượt phải thuộc vé tháng đúng tháng gửi. Các khoảng gửi không chồng lấn.
-- Phí được tính động trong view, không lưu để tránh sai khi sửa/xóa lịch sử.
CREATE PROCEDURE sp_kiem_tra_luot(
 IN p_bienso VARCHAR(15), IN p_thang DATE, IN p_gui DATETIME, IN p_lay DATETIME,
 IN o_bienso VARCHAR(15), IN o_thang DATE, IN o_gui DATETIME)
BEGIN
 IF p_thang IS NULL OR p_gui IS NULL OR p_thang <> DATE_FORMAT(p_gui, '%Y-%m-01') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thang ve khong khop thoi gian gui';
 END IF;
 IF p_lay IS NOT NULL AND p_lay < p_gui THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Thoi gian lay truoc thoi gian gui';
 END IF;
 IF EXISTS (SELECT 1 FROM LUOT_GUI_LAY_XE l
  WHERE l.BienSo = p_bienso
   AND NOT (l.BienSo <=> o_bienso AND l.ThangDangKy <=> o_thang AND l.ThoiGianGui <=> o_gui)
   AND (l.ThoiGianGui = p_gui OR
     ((p_lay IS NULL OR l.ThoiGianGui < p_lay)
      AND (l.ThoiGianLay IS NULL OR l.ThoiGianLay > p_gui)))) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Luot gui bi trung hoac chong lan';
 END IF;
END$$
CREATE TRIGGER trg_luot_bi BEFORE INSERT ON LUOT_GUI_LAY_XE FOR EACH ROW
BEGIN
 CALL sp_kiem_tra_luot(NEW.BienSo, NEW.ThangDangKy, NEW.ThoiGianGui, NEW.ThoiGianLay, NULL, NULL, NULL);
END$$
CREATE TRIGGER trg_luot_bu BEFORE UPDATE ON LUOT_GUI_LAY_XE FOR EACH ROW
BEGIN
 CALL sp_kiem_tra_luot(NEW.BienSo, NEW.ThangDangKy, NEW.ThoiGianGui, NEW.ThoiGianLay, OLD.BienSo, OLD.ThangDangKy, OLD.ThoiGianGui);
END$$

DELIMITER ;

-- NHẬP DỮ LIỆU MẪU

INSERT INTO NGUOI (CCCD, HoTen, NgaySinh) VALUES
-- Sinh viên
('001205010001', 'Nguyễn Văn An',  '2005-03-12'),
('001205010002', 'Trần Quốc Bình', '2005-07-21'),
('001206010003', 'Lê Thị Chi',     '2006-01-09'),
('001205010004', 'Phạm Đức Dũng',  '2005-11-30'),
('001206010005', 'Hoàng Thu Hà',   '2006-05-17'),
('001204010006', 'Vũ Mạnh Hùng',   '2004-09-02'),
('001205010007', 'Đặng Ngọc Lan',  '2005-12-25'),
('001206010008', 'Bùi Quang Minh', '2006-04-08'),
-- Khách đến chơi
('038178000011', 'Nguyễn Thị Hoa', '1978-02-14'),
('001204000012', 'Trần Văn Nam',   '2004-06-06'),
('001205000013', 'Lê Hồng Nhung',  '2005-08-19'),
('038175000014', 'Phạm Minh Tuấn', '1975-10-03');

INSERT INTO SINH_VIEN (MaSV, CCCD, Lop, QueQuan) VALUES
('B24DCCN001', '001205010001', 'D24CQCN01-B', 'Hà Nam'),
('B24DCVT002', '001205010002', 'D24CQVT02-B', 'Nam Định'),
('B24DCCN003', '001206010003', 'D24CQCN03-B', 'Thanh Hóa'),
('B24DCDK004', '001205010004', 'D24CQDK01-B', 'Nghệ An'),
('B24DCCN005', '001206010005', 'D24CQCN05-B', 'Hải Phòng'),
('B24DCMR006', '001204010006', 'D24CQMR01-B', 'Thái Bình'),
('B24DCDT007', '001205010007', 'D24CQDT01-B', 'Bắc Ninh'),
('B24DCCN008', '001206010008', 'D24CQCN08-B', 'Hà Tĩnh');

INSERT INTO KHACH (CCCD) VALUES
('038178000011'),
('001204000012'),
('001205000013'),
('038175000014');

INSERT INTO PHONG (SoPhong, LoaiPhong, DonGia, SoNguoiToiDa) VALUES
('A101', 'Thường',  500000, 8),
('A102', 'Thường',  500000, 8),
('B201', 'Cao cấp', 900000, 4),
('B202', 'Cao cấp', 900000, 4),
('C301', 'Thường',  500000, 8);

-- Lưu ý: B24DCDK004 chuyển từ phòng A102 sang B201 vào giữa tháng 10 (ngày 2026-10-15)
-- Theo quy ước [NgayBatDau, NgayKetThuc), đợt cũ kết thúc 2026-10-15 và đợt mới bắt đầu 2026-10-15 không bị coi là chồng lấn,
-- và tiền phòng tháng 10 của B24DCDK004 sẽ lấy 1 lần theo đơn giá phòng B201 (đợt có NgayBatDau lớn nhất trong tháng 10).
INSERT INTO O_PHONG (MaSV, SoPhong, NgayBatDau, NgayKetThuc) VALUES
('B24DCCN001', 'A101', '2026-08-25', NULL),
('B24DCVT002', 'A101', '2026-08-25', NULL),
('B24DCCN003', 'A101', '2026-09-01', NULL),
('B24DCDK004', 'A102', '2026-08-25', '2026-10-15'),
('B24DCDK004', 'B201', '2026-10-15', NULL),
('B24DCCN005', 'A102', '2026-09-01', NULL),
('B24DCMR006', 'B201', '2026-09-01', NULL),
('B24DCDT007', 'B202', '2026-09-10', NULL),
('B24DCCN008', 'C301', '2026-10-05', NULL);

INSERT INTO DICH_VU (MaDV, TenDV, DonGia) VALUES
('DV01', 'Giặt là(kg)',         20000),
('DV02', 'Trông xe',             3000),
('DV03', 'Cho thuê xe(giờ)',    30000),
('DV04', 'Ăn uống(suất)',       25000),
('DV05', 'Nước uống đóng bình', 20000);

INSERT INTO LAN_SU_DUNG (MaSV, MaDV, ThoiGianSD, SoLuong) VALUES
('B24DCCN001', 'DV01', '2026-09-05 08:30:00', 3),
('B24DCCN001', 'DV01', '2026-09-19 09:00:00', 2),
('B24DCCN001', 'DV04', '2026-09-12 12:15:00', 4),
('B24DCCN001', 'DV01', '2026-10-03 08:10:00', 4),
('B24DCCN001', 'DV04', '2026-10-10 18:30:00', 2),
('B24DCVT002', 'DV01', '2026-09-07 20:00:00', 2),
('B24DCVT002', 'DV03', '2026-09-20 14:00:00', 3),
('B24DCVT002', 'DV01', '2026-10-04 19:30:00', 5),
('B24DCCN003', 'DV04', '2026-09-15 12:00:00', 10),
('B24DCCN003', 'DV01', '2026-10-11 07:45:00', 3),
('B24DCDK004', 'DV03', '2026-08-30 10:00:00', 2),
('B24DCDK004', 'DV01', '2026-09-25 08:00:00', 2),
('B24DCCN005', 'DV04', '2026-10-02 18:00:00', 6),
('B24DCCN005', 'DV01', '2026-10-16 09:30:00', 2),
('B24DCMR006', 'DV03', '2026-10-14 15:00:00', 4),
('B24DCDT007', 'DV04', '2026-10-06 12:30:00', 8),
('B24DCCN008', 'DV01', '2026-10-18 08:20:00', 3);

INSERT INTO LUOT_THAM (CCCD_Khach, MaSV, NgayDenChoi) VALUES
('038178000011', 'B24DCCN001', '2026-10-04 15:00:00'),
('038178000011', 'B24DCCN001', '2026-10-11 15:30:00'),
('038178000011', 'B24DCCN001', '2026-10-20 16:00:00'),
('001204000012', 'B24DCCN001', '2026-10-20 19:00:00'),
('001205000013', 'B24DCCN003', '2026-10-19 14:00:00'),
('001205000013', 'B24DCCN003', '2026-10-21 10:00:00'),
('001205000013', 'B24DCDT007', '2026-10-21 09:00:00'),
('038175000014', 'B24DCCN005', '2026-09-27 17:00:00'),
('038175000014', 'B24DCCN005', '2026-10-08 17:00:00'),
('001204000012', 'B24DCMR006', '2026-09-15 20:00:00');

INSERT INTO XE (BienSo, LoaiXe, MaSV) VALUES
('29B1-111.11', 'Xe máy',      'B24DCCN001'),
('29B1-222.22', 'Xe đạp điện', 'B24DCCN001'),
('29C1-333.33', 'Xe máy',      'B24DCVT002'),
('29D1-444.44', 'Xe máy',      'B24DCCN003'),
('29E1-555.55', 'Xe máy',      'B24DCCN005'),
('29F1-666.66', 'Xe đạp điện', 'B24DCMR006'),
('29G1-777.77', 'Xe máy',      'B24DCDT007');

INSERT INTO VE_THANG (BienSo, ThangDangKy, DonGiaVe) VALUES
('29B1-111.11', '2026-09-01', 100000),
('29B1-111.11', '2026-10-01', 100000),
('29C1-333.33', '2026-09-01', 100000),
('29C1-333.33', '2026-10-01', 100000),
('29D1-444.44', '2026-09-01', 100000),
('29D1-444.44', '2026-10-01', 100000),
('29E1-555.55', '2026-09-01', 100000),
('29E1-555.55', '2026-10-01', 100000),
('29B1-222.22', '2026-10-01', 100000),
('29F1-666.66', '2026-10-01', 100000),
('29G1-777.77', '2026-10-01', 100000);

-- Nhập lượt gửi/lấy xe; xem SoTienPhaiTra đã tính tại V_LUOT_GUI_LAY_XE.
INSERT INTO LUOT_GUI_LAY_XE (BienSo, ThangDangKy, ThoiGianGui, ThoiGianLay) VALUES
('29B1-111.11', '2026-09-01', '2026-09-14 07:00:00', '2026-09-14 07:30:00'), -- Lượt 1: 0đ
('29B1-111.11', '2026-09-01', '2026-09-14 12:00:00', '2026-09-14 12:20:00'), -- Lượt 2: 0đ
('29B1-111.11', '2026-09-01', '2026-09-14 18:00:00', '2026-09-14 18:30:00'), -- Lượt 3: 3000đ
('29B1-111.11', '2026-10-01', '2026-10-12 07:00:00', '2026-10-12 07:30:00'), -- Lượt 1: 0đ
('29B1-111.11', '2026-10-01', '2026-10-12 12:00:00', '2026-10-12 12:20:00'), -- Lượt 2: 0đ
('29B1-111.11', '2026-10-01', '2026-10-12 17:30:00', '2026-10-12 18:00:00'), -- Lượt 3: 3000đ
('29B1-111.11', '2026-10-01', '2026-10-12 21:00:00', '2026-10-12 21:15:00'), -- Lượt 4: 3000đ
('29B1-111.11', '2026-10-01', '2026-10-13 07:10:00', '2026-10-13 07:40:00'), -- Lượt 1 ngày mới: 0đ
('29C1-333.33', '2026-10-01', '2026-10-12 08:00:00', '2026-10-12 11:00:00'), -- Lượt 1: 0đ
('29C1-333.33', '2026-10-01', '2026-10-12 13:00:00', '2026-10-12 19:00:00'), -- Lượt 2: 0đ
('29D1-444.44', '2026-10-01', '2026-10-15 07:00:00', '2026-10-15 09:00:00'), -- Lượt 1: 0đ
('29D1-444.44', '2026-10-01', '2026-10-15 12:00:00', '2026-10-15 13:00:00'), -- Lượt 2: 0đ
('29D1-444.44', '2026-10-01', '2026-10-15 17:00:00', '2026-10-15 22:00:00'), -- Lượt 3: 3000đ
('29G1-777.77', '2026-10-01', '2026-10-16 08:00:00', NULL);                  -- Xe đang gửi trong bãi: 0đ


-- BỐN TRUY VẤN CỦA YÊU CẦU 2
DELIMITER $$

-- Kiểm tra tháng hợp lệ trước khi truy vấn
CREATE PROCEDURE sp_kiem_tra_thang_truy_van(IN p_thang DATE)
BEGIN
    IF p_thang IS NULL OR p_thang >= '9999-12-01' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tháng truy vấn không hợp lệ';
    END IF;
END$$

-- Đối chiếu danh mục khi có vé xe trong khoảng được hỏi.
-- File này dùng tên 'Trông xe' để nhận diện dịch vụ do bảng DICH_VU chưa có cột phân loại. Báo lỗi thay vì âm thầm bỏ sót tiền vé/phụ phí nếu danh mục trông xe đã bị đổi tên hoặc xóa.
CREATE PROCEDURE sp_kiem_tra_danh_muc_trong_xe(IN p_tu DATE, IN p_den DATE)
BEGIN
    IF EXISTS (
        SELECT 1 FROM VE_THANG v
        WHERE v.ThangDangKy <= p_den AND LAST_DAY(v.ThangDangKy) >= p_tu
    ) AND NOT EXISTS (SELECT 1 FROM DICH_VU WHERE TenDV = 'Trông xe') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Thieu dich vu Trong xe trong danh muc DICH_VU';
    END IF;
END$$

-- Truy vấn 1: Liệt kê thông tin sinh viên cùng số tiền phải trả cho tất cả dịch vụ (gồm cả tiền phòng) trong mỗi tháng.
-- Hỗ trợ in theo danh sách (truyền p_masv = NULL) hoặc theo từng người (truyền mã SV cụ thể).
CREATE PROCEDURE sp_hoa_don_thang(IN p_thang DATE, IN p_masv VARCHAR(10))
BEGIN
    DECLARE v_bd DATE;
    DECLARE v_sau DATE;
    CALL sp_kiem_tra_thang_truy_van(p_thang);
    SET v_bd = DATE_SUB(p_thang, INTERVAL (DAY(p_thang) - 1) DAY);
    SET v_sau = DATE_ADD(v_bd, INTERVAL 1 MONTH);
    CALL sp_kiem_tra_danh_muc_trong_xe(v_bd, LAST_DAY(v_bd));

    WITH phong_xep AS (
        SELECT o.MaSV, o.SoPhong, p.DonGia,
               ROW_NUMBER() OVER (PARTITION BY o.MaSV ORDER BY o.NgayBatDau DESC, o.SoPhong) AS stt
        FROM O_PHONG o
        JOIN PHONG p ON p.SoPhong = o.SoPhong
        WHERE o.NgayBatDau < v_sau AND (o.NgayKetThuc IS NULL OR o.NgayKetThuc > v_bd)
    ),
    dv AS (
        SELECT l.MaSV, SUM(l.SoLuong * d.DonGia) AS SoTien
        FROM LAN_SU_DUNG l
        JOIN DICH_VU d ON d.MaDV = l.MaDV
        WHERE l.ThoiGianSD >= v_bd AND l.ThoiGianSD < v_sau
          AND d.TenDV <> 'Trông xe'
        GROUP BY l.MaSV
    ),
    ve AS (
        SELECT x.MaSV, SUM(v.DonGiaVe) AS SoTien
        FROM XE x
        JOIN VE_THANG v ON v.BienSo = x.BienSo
        WHERE v.ThangDangKy = v_bd
        GROUP BY x.MaSV
    ),
    gui AS (
        SELECT x.MaSV, SUM(l.SoTienPhaiTra) AS SoTien
        FROM XE x
        JOIN V_LUOT_GUI_LAY_XE l ON l.BienSo = x.BienSo
        WHERE l.ThoiGianGui >= v_bd AND l.ThoiGianGui < v_sau
        GROUP BY x.MaSV
    )
    SELECT v_bd AS Thang, s.MaSV, n.CCCD, n.HoTen, n.NgaySinh, s.Lop, s.QueQuan,
           p.SoPhong AS PhongTinhTien,
           COALESCE(p.DonGia, 0) AS TienPhong,
           COALESCE(dv.SoTien, 0) AS TienDichVu,
           COALESCE(ve.SoTien, 0) AS TienVeThang,
           COALESCE(gui.SoTien, 0) AS PhuPhiGuiXe,
           COALESCE(p.DonGia, 0) + COALESCE(dv.SoTien, 0) + COALESCE(ve.SoTien, 0) + COALESCE(gui.SoTien, 0) AS TongPhaiTra
    FROM SINH_VIEN s
    JOIN NGUOI n ON n.CCCD = s.CCCD
    LEFT JOIN phong_xep p ON p.MaSV = s.MaSV AND p.stt = 1
    LEFT JOIN dv ON dv.MaSV = s.MaSV
    LEFT JOIN ve ON ve.MaSV = s.MaSV
    LEFT JOIN gui ON gui.MaSV = s.MaSV
    WHERE (p.MaSV IS NOT NULL OR dv.MaSV IS NOT NULL OR ve.MaSV IS NOT NULL OR gui.MaSV IS NOT NULL)
      AND (p_masv IS NULL OR s.MaSV = p_masv)
    ORDER BY s.MaSV;
END$$

-- Truy vấn 2: Liệt kê thông tin sinh viên cùng tên dịch vụ, tổng giá mỗi dịch vụ sử dụng từ ngày bắt đầu đến ngày kết thúc.
CREATE PROCEDURE sp_dich_vu_trong_khoang(IN p_tu DATE, IN p_den DATE)
BEGIN
    IF p_tu IS NULL OR p_den IS NULL OR p_den < p_tu OR p_den >= '9999-12-31' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Khoảng ngày không hợp lệ';
    END IF;
    CALL sp_kiem_tra_danh_muc_trong_xe(p_tu, p_den);

    WITH phat_sinh AS (
        SELECT l.MaSV, l.MaDV, l.SoLuong * d.DonGia AS SoTien
        FROM LAN_SU_DUNG l
        JOIN DICH_VU d ON d.MaDV = l.MaDV AND d.TenDV <> 'Trông xe'
        WHERE l.ThoiGianSD >= p_tu AND l.ThoiGianSD < DATE_ADD(p_den, INTERVAL 1 DAY)
        UNION ALL
        SELECT x.MaSV, d.MaDV, v.DonGiaVe
        FROM XE x
        JOIN VE_THANG v ON v.BienSo = x.BienSo
        JOIN DICH_VU d ON d.TenDV = 'Trông xe'
        WHERE v.ThangDangKy <= p_den AND LAST_DAY(v.ThangDangKy) >= p_tu
        UNION ALL
        SELECT x.MaSV, d.MaDV, l.SoTienPhaiTra
        FROM XE x
        JOIN V_LUOT_GUI_LAY_XE l ON l.BienSo = x.BienSo
        JOIN DICH_VU d ON d.TenDV = 'Trông xe'
        WHERE l.ThoiGianGui >= p_tu AND l.ThoiGianGui < DATE_ADD(p_den, INTERVAL 1 DAY)
    )
    SELECT s.MaSV, n.CCCD, n.HoTen, n.NgaySinh, s.Lop, s.QueQuan,
           d.MaDV, d.TenDV, SUM(ps.SoTien) AS TongTien
    FROM phat_sinh ps
    JOIN SINH_VIEN s ON s.MaSV = ps.MaSV
    JOIN NGUOI n ON n.CCCD = s.CCCD
    JOIN DICH_VU d ON d.MaDV = ps.MaDV
    GROUP BY s.MaSV, n.CCCD, n.HoTen, n.NgaySinh, s.Lop, s.QueQuan, d.MaDV, d.TenDV
    ORDER BY s.MaSV, d.MaDV;
END$$

-- Truy vấn 3: Liệt kê thông tin sinh viên cùng thông tin khách đến thăm trong tuần hoặc tháng, cùng số lần đến chơi.
CREATE PROCEDURE sp_khach_tham(IN p_tu DATE, IN p_den DATE)
BEGIN
    IF p_tu IS NULL OR p_den IS NULL OR p_den < p_tu OR p_den >= '9999-12-31' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Khoảng ngày không hợp lệ';
    END IF;

    SELECT s.MaSV, ns.CCCD AS CCCD_SinhVien, ns.HoTen AS TenSinhVien,
           ns.NgaySinh AS NgaySinhSinhVien, s.Lop, s.QueQuan,
           nk.CCCD AS CCCD_Khach, nk.HoTen AS TenKhach, nk.NgaySinh AS NgaySinhKhach,
           COUNT(*) AS SoLanTham
    FROM LUOT_THAM l
    JOIN SINH_VIEN s ON s.MaSV = l.MaSV
    JOIN NGUOI ns ON ns.CCCD = s.CCCD
    JOIN KHACH k ON k.CCCD = l.CCCD_Khach
    JOIN NGUOI nk ON nk.CCCD = k.CCCD
    WHERE l.NgayDenChoi >= p_tu AND l.NgayDenChoi < DATE_ADD(p_den, INTERVAL 1 DAY)
    GROUP BY s.MaSV, ns.CCCD, ns.HoTen, ns.NgaySinh, s.Lop, s.QueQuan, nk.CCCD, nk.HoTen, nk.NgaySinh
    ORDER BY s.MaSV, SoLanTham DESC, nk.CCCD;
END$$

-- Truy vấn 4: Liệt kê danh mục các dịch vụ cùng doanh thu của mỗi dịch vụ trong KTX trong mỗi tháng.
CREATE PROCEDURE sp_doanh_thu_dich_vu(IN p_thang DATE)
BEGIN
    DECLARE v_bd DATE;
    DECLARE v_sau DATE;
    CALL sp_kiem_tra_thang_truy_van(p_thang);
    SET v_bd = DATE_SUB(p_thang, INTERVAL (DAY(p_thang) - 1) DAY);
    SET v_sau = DATE_ADD(v_bd, INTERVAL 1 MONTH);
    CALL sp_kiem_tra_danh_muc_trong_xe(v_bd, LAST_DAY(v_bd));

    WITH dich_vu_thuong AS (
        SELECT l.MaDV, SUM(l.SoLuong * d.DonGia) AS SoTien
        FROM LAN_SU_DUNG l
        JOIN DICH_VU d ON d.MaDV = l.MaDV AND d.TenDV <> 'Trông xe'
        WHERE l.ThoiGianSD >= v_bd AND l.ThoiGianSD < v_sau
        GROUP BY l.MaDV
    ),
    ve AS (
        SELECT COALESCE(SUM(DonGiaVe), 0) AS SoTien
        FROM VE_THANG
        WHERE ThangDangKy = v_bd
    ),
    gui AS (
        SELECT COALESCE(SUM(SoTienPhaiTra), 0) AS SoTien
        FROM V_LUOT_GUI_LAY_XE
        WHERE ThoiGianGui >= v_bd AND ThoiGianGui < v_sau
    )
    SELECT v_bd AS Thang, d.MaDV, d.TenDV, d.DonGia AS DonGiaNiemYet,
           CASE WHEN d.TenDV = 'Trông xe' THEN ve.SoTien + gui.SoTien
                ELSE COALESCE(dt.SoTien, 0) END AS DoanhThu
    FROM DICH_VU d
    LEFT JOIN dich_vu_thuong dt ON dt.MaDV = d.MaDV
    CROSS JOIN ve
    CROSS JOIN gui
    ORDER BY d.MaDV;
END$$

DELIMITER ;

-- GỌI THỰC THI 4 TRUY VẤN ĐỂ HIỂN THỊ BẢNG KẾT QUẢ RA MÀN HÌNH

CALL sp_hoa_don_thang('2026-10-01', NULL);                 -- Truy vấn 1 (In danh sách cả KTX tháng 10/2026)
CALL sp_hoa_don_thang('2026-10-01', 'B24DCCN001');         -- Truy vấn 1 (In riêng 1 sinh viên)
CALL sp_dich_vu_trong_khoang('2026-09-01', '2026-10-31');  -- Truy vấn 2 (Tổng tiền dịch vụ từ 01/09 đến 31/10)
CALL sp_khach_tham('2026-10-01', '2026-10-31');            -- Truy vấn 3 (Khách thăm trong tháng 10/2026)
CALL sp_khach_tham('2026-10-19', '2026-10-25');            -- Truy vấn 3 (Khách thăm trong tuần 19/10 - 25/10)
CALL sp_doanh_thu_dich_vu('2026-10-01');                   -- Truy vấn 4 (Doanh thu từng dịch vụ tháng 10/2026)