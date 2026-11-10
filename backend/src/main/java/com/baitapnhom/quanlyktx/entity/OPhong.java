package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "O_PHONG")
@IdClass(OPhongId.class)
public class OPhong {

    @Id
    @Column(name = "MaSV", length = 10)
    private String maSV;

    @Id
    @Column(name = "SoPhong", length = 10)
    private String soPhong;

    @Id
    @Column(name = "NgayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc")
    private LocalDate ngayKetThuc;

    public OPhong() {
    }

    public OPhong(String maSV, String soPhong, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maSV = maSV;
        this.soPhong = soPhong;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

}
