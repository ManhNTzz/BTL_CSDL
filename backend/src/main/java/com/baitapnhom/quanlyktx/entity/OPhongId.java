package com.baitapnhom.quanlyktx.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class OPhongId implements Serializable {

    private String maSV;
    private String soPhong;
    private LocalDate ngayBatDau;

    public OPhongId() {
    }

    public OPhongId(String maSV, String soPhong, LocalDate ngayBatDau) {
        this.maSV = maSV;
        this.soPhong = soPhong;
        this.ngayBatDau = ngayBatDau;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OPhongId other)) {
            return false;
        }
        return Objects.equals(maSV, other.maSV) && Objects.equals(soPhong, other.soPhong) && Objects.equals(ngayBatDau, other.ngayBatDau);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maSV, soPhong, ngayBatDau);
    }
}
