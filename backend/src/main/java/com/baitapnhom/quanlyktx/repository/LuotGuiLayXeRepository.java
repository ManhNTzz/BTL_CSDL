package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.entity.LuotGuiLayXe;
import com.baitapnhom.quanlyktx.entity.LuotGuiLayXeId;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LuotGuiLayXeRepository extends JpaRepository<LuotGuiLayXe, LuotGuiLayXeId> {

    List<LuotGuiLayXe> findByBienSo(String bienSo);

    List<LuotGuiLayXe> findByThoiGianLayIsNull();

    List<LuotGuiLayXe> findByBienSoAndThoiGianGuiBetween(String bienSo, LocalDateTime tu, LocalDateTime den);
}
