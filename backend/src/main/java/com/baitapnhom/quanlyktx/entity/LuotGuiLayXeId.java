package com.baitapnhom.quanlyktx.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class LuotGuiLayXeId implements Serializable {

    private String bienSo;
    private LocalDate thangDangKy;
    private LocalDateTime thoiGianGui;

    public LuotGuiLayXeId() {
    }

    public LuotGuiLayXeId(String bienSo, LocalDate thangDangKy, LocalDateTime thoiGianGui) {
        this.bienSo = bienSo;
        this.thangDangKy = thangDangKy;
        this.thoiGianGui = thoiGianGui;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LuotGuiLayXeId other)) {
            return false;
        }
        return Objects.equals(bienSo, other.bienSo)
                && Objects.equals(thangDangKy, other.thangDangKy)
                && Objects.equals(thoiGianGui, other.thoiGianGui);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bienSo, thangDangKy, thoiGianGui);
    }
}
