package com.baitapnhom.quanlyktx.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class LuotThamId implements Serializable {

    private String cccdKhach;
    private String maSV;
    private LocalDateTime ngayDenChoi;

    public LuotThamId() {
    }

    public LuotThamId(String cccdKhach, String maSV, LocalDateTime ngayDenChoi) {
        this.cccdKhach = cccdKhach;
        this.maSV = maSV;
        this.ngayDenChoi = ngayDenChoi;
    }

    public String getCccdKhach() {
        return cccdKhach;
    }

    public void setCccdKhach(String cccdKhach) {
        this.cccdKhach = cccdKhach;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public LocalDateTime getNgayDenChoi() {
        return ngayDenChoi;
    }

    public void setNgayDenChoi(LocalDateTime ngayDenChoi) {
        this.ngayDenChoi = ngayDenChoi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LuotThamId other)) {
            return false;
        }
        return Objects.equals(cccdKhach, other.cccdKhach) && Objects.equals(maSV, other.maSV) && Objects.equals(ngayDenChoi, other.ngayDenChoi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cccdKhach, maSV, ngayDenChoi);
    }
}
