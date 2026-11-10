package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LUOT_GUI_LAY_XE")
@IdClass(LuotGuiLayXeId.class)
public class LuotGuiLayXe {

    @Id
    @Column(name = "BienSo", length = 15)
    private String bienSo;

    @Id
    @Column(name = "ThangDangKy")
    private LocalDate thangDangKy;

    @Id
    @Column(name = "ThoiGianGui")
    private LocalDateTime thoiGianGui;

    @Column(name = "ThoiGianLay")
    private LocalDateTime thoiGianLay;

    public LuotGuiLayXe() {
    }

    public LuotGuiLayXe(String bienSo, LocalDate thangDangKy, LocalDateTime thoiGianGui, LocalDateTime thoiGianLay) {
        this.bienSo = bienSo;
        this.thangDangKy = thangDangKy;
        this.thoiGianGui = thoiGianGui;
        this.thoiGianLay = thoiGianLay;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String bienSo) {
        this.bienSo = bienSo;
    }

    public LocalDate getThangDangKy() {
        return thangDangKy;
    }

    public void setThangDangKy(LocalDate thangDangKy) {
        this.thangDangKy = thangDangKy;
    }

    public LocalDateTime getThoiGianGui() {
        return thoiGianGui;
    }

    public void setThoiGianGui(LocalDateTime thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }

    public LocalDateTime getThoiGianLay() {
        return thoiGianLay;
    }

    public void setThoiGianLay(LocalDateTime thoiGianLay) {
        this.thoiGianLay = thoiGianLay;
    }
}
