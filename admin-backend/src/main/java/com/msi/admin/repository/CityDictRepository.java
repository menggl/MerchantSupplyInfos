package com.msi.admin.repository;

import com.msi.admin.domain.CityDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityDictRepository extends JpaRepository<CityDict, Long> {
    Optional<CityDict> findByCityCode(String cityCode);
    List<CityDict> findByValidOrderBySortAsc(Integer valid);
    List<CityDict> findAllByOrderBySortAsc();
    @Query("select max(c.sort) from CityDict c")
    Integer findMaxSort();
}
