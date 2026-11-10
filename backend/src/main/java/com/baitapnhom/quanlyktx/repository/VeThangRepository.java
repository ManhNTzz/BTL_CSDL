package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.entity.VeThang;
import com.baitapnhom.quanlyktx.entity.VeThangId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeThangRepository extends JpaRepository<VeThang, VeThangId> {

    List<VeThang> findByThangDangKy(LocalDate thangDangKy);
}
