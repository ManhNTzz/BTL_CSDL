package com.baitapnhom.quanlyktx.repository;

import com.baitapnhom.quanlyktx.entity.LuotTham;
import com.baitapnhom.quanlyktx.entity.LuotThamId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LuotThamRepository extends JpaRepository<LuotTham, LuotThamId> {
}
