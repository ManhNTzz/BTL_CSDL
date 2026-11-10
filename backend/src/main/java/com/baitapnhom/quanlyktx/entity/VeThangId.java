package com.baitapnhom.quanlyktx.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class VeThangId implements Serializable {

    private String bienSo;
    private LocalDate thangDangKy;

    public VeThangId() {
    }

    public VeThangId(String bienSo, LocalDate thangDangKy) {
        this.bienSo = bienSo;
        this.thangDangKy = thangDangKy;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VeThangId other)) {
            return false;
        }
        return Objects.equals(bienSo, other.bienSo) && Objects.equals(thangDangKy, other.thangDangKy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bienSo, thangDangKy);
    }
}
