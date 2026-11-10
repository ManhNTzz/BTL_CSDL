package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "DICH_VU")
public class DichVu {

    @Id
    @Column(name = "MaDV", length = 10)
    private String maDV;

    @Column(name = "TenDV", nullable = false, length = 50)
    private String tenDV;

    @Column(name = "DonGia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    public DichVu() {
    }

    public DichVu(String maDV, String tenDV, BigDecimal donGia) {
        this.maDV = maDV;
        this.tenDV = tenDV;
        this.donGia = donGia;
    }

    public String getMaDV() {
        return maDV;
    }

    public String getTenDV() {
        return tenDV;
    }

    public void setTenDV(String tenDV) {
        this.tenDV = tenDV;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

}
