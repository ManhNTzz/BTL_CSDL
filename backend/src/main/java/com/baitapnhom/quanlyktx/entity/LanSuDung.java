package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LAN_SU_DUNG")
@IdClass(LanSuDungId.class)
public class LanSuDung {

    @Id
    @Column(name = "MaSV", length = 10)
    private String maSV;

    @Id
    @Column(name = "MaDV", length = 10)
    private String maDV;

    @Id
    @Column(name = "ThoiGianSD")
    private LocalDateTime thoiGianSD;

    @Column(name = "SoLuong", nullable = false)
    private Integer soLuong;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal donGia;

    public LanSuDung() {
    }

    public LanSuDung(String maSV, String maDV, LocalDateTime thoiGianSD, Integer soLuong) {
        this(maSV, maDV, thoiGianSD, soLuong, BigDecimal.ZERO);
    }

    public LanSuDung(String maSV, String maDV, LocalDateTime thoiGianSD, Integer soLuong, BigDecimal donGia) {
        this.maSV = maSV;
        this.maDV = maDV;
        this.thoiGianSD = thoiGianSD;
        this.soLuong = soLuong;
        this.donGia = donGia != null ? donGia : BigDecimal.ZERO;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getMaDV() {
        return maDV;
    }

    public LocalDateTime getThoiGianSD() {
        return thoiGianSD;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }
}
