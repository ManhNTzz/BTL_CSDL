package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.entity.Xe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XeRepository extends JpaRepository<Xe, String> {

    long countByMaSV(String maSV);
}
