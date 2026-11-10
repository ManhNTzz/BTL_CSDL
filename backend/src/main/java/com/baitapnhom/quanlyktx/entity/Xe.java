package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "XE")
public class Xe {

    @Id
    @Column(name = "BienSo", length = 15)
    private String bienSo;

    @Column(name = "LoaiXe", nullable = false, length = 30)
    private String loaiXe;

    @Column(name = "MaSV", nullable = false, length = 10)
    private String maSV;

    public Xe() {
    }

    public Xe(String bienSo, String loaiXe, String maSV) {
        this.bienSo = bienSo;
        this.loaiXe = loaiXe;
        this.maSV = maSV;
    }

    public String getBienSo() {
        return bienSo;
    }

    public String getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(String loaiXe) {
        this.loaiXe = loaiXe;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

}
