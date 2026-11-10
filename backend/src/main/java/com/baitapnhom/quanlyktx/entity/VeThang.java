package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "VE_THANG")
@IdClass(VeThangId.class)
public class VeThang {

    @Id
    @Column(name = "BienSo", length = 15)
    private String bienSo;

    @Id
    @Column(name = "ThangDangKy")
    private LocalDate thangDangKy;

    @Column(name = "DonGiaVe", nullable = false, precision = 10, scale = 0)
    private BigDecimal donGiaVe;

    public VeThang() {
    }

    public VeThang(String bienSo, LocalDate thangDangKy, BigDecimal donGiaVe) {
        this.bienSo = bienSo;
        this.thangDangKy = thangDangKy;
        this.donGiaVe = donGiaVe;
    }

    public String getBienSo() {
        return bienSo;
    }

    public LocalDate getThangDangKy() {
        return thangDangKy;
    }

    public BigDecimal getDonGiaVe() {
        return donGiaVe;
    }

    public void setDonGiaVe(BigDecimal donGiaVe) {
        this.donGiaVe = donGiaVe;
    }

}
