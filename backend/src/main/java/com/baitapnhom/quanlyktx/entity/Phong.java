package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "PHONG")
public class Phong {

    @Id
    @Column(name = "SoPhong", length = 10)
    private String soPhong;

    @Column(name = "LoaiPhong", nullable = false, length = 30)
    private String loaiPhong;

    @Column(name = "DonGia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    @Column(name = "SoNguoiToiDa", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer soNguoiToiDa;

    public Phong() {
    }

    public Phong(String soPhong, String loaiPhong, BigDecimal donGia, Integer soNguoiToiDa) {
        this.soPhong = soPhong;
        this.loaiPhong = loaiPhong;
        this.donGia = donGia;
        this.soNguoiToiDa = soNguoiToiDa;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public Integer getSoNguoiToiDa() {
        return soNguoiToiDa;
    }

    public void setSoNguoiToiDa(Integer soNguoiToiDa) {
        this.soNguoiToiDa = soNguoiToiDa;
    }

}
