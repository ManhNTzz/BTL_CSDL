package com.baitapnhom.quanlyktx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "KHACH")
public class Khach {

    @Id
    @Column(name = "CCCD", length = 12, columnDefinition = "CHAR(12)")
    private String cccd;

    public Khach() {
    }

    public Khach(String cccd) {
        this.cccd = cccd;
    }

    public String getCccd() {
        return cccd;
    }

}
