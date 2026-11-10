package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "LUOT_THAM")
@IdClass(LuotThamId.class)
public class LuotTham {

    @Id
    @Column(name = "CCCD_Khach", length = 12, columnDefinition = "CHAR(12)")
    private String cccdKhach;

    @Id
    @Column(name = "MaSV", length = 10)
    private String maSV;

    @Id
    @Column(name = "NgayDenChoi")
    private LocalDateTime ngayDenChoi;

    public LuotTham() {
    }

    public LuotTham(String cccdKhach, String maSV, LocalDateTime ngayDenChoi) {
        this.cccdKhach = cccdKhach;
        this.maSV = maSV;
        this.ngayDenChoi = ngayDenChoi;
    }

    public String getCccdKhach() {
        return cccdKhach;
    }

    public String getMaSV() {
        return maSV;
    }

    public LocalDateTime getNgayDenChoi() {
        return ngayDenChoi;
    }

}
