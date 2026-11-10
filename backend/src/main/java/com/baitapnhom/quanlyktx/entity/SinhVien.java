package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SINH_VIEN")
public class SinhVien {

    @Id
    @Column(name = "MaSV", length = 10)
    private String maSV;

    @Column(name = "CCCD", nullable = false, length = 12, columnDefinition = "CHAR(12)")
    private String cccd;

    @Column(name = "Lop", nullable = false, length = 20)
    private String lop;

    @Column(name = "QueQuan", nullable = false, length = 100)
    private String queQuan;

    public SinhVien() {
    }

    public SinhVien(String maSV, String cccd, String lop, String queQuan) {
        this.maSV = maSV;
        this.cccd = cccd;
        this.lop = lop;
        this.queQuan = queQuan;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

}
