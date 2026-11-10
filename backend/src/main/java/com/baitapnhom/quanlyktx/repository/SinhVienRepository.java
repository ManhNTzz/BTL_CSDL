package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.entity.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {

    boolean existsByCccd(String cccd);
}
