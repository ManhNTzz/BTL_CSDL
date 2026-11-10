package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.entity.OPhong;
import com.baitapnhom.quanlyktx.entity.OPhongId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OPhongRepository extends JpaRepository<OPhong, OPhongId> {

    List<OPhong> findByMaSV(String maSV);
    List<OPhong> findBySoPhong(String soPhong);
}
