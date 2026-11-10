package com.baitapnhom.quanlyktx.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class LanSuDungId implements Serializable {

    private String maSV;
    private String maDV;
    private LocalDateTime thoiGianSD;

    public LanSuDungId() {
    }

    public LanSuDungId(String maSV, String maDV, LocalDateTime thoiGianSD) {
        this.maSV = maSV;
        this.maDV = maDV;
        this.thoiGianSD = thoiGianSD;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getMaDV() {
        return maDV;
    }

    public void setMaDV(String maDV) {
        this.maDV = maDV;
    }

    public LocalDateTime getThoiGianSD() {
        return thoiGianSD;
    }

    public void setThoiGianSD(LocalDateTime thoiGianSD) {
        this.thoiGianSD = thoiGianSD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LanSuDungId other)) {
            return false;
        }
        return Objects.equals(maSV, other.maSV) && Objects.equals(maDV, other.maDV) && Objects.equals(thoiGianSD, other.thoiGianSD);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSV, maDV, thoiGianSD);
    }
}
