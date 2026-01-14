package com.msi.admin.repository;

import com.msi.admin.domain.PhoneRemarkDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneRemarkDictRepository extends JpaRepository<PhoneRemarkDict, Long> {
  List<PhoneRemarkDict> findByTypeOrderBySortAsc(Integer type);
  List<PhoneRemarkDict> findAllByOrderByTypeAscSortAsc();

  @Query("SELECT MAX(p.sort) FROM PhoneRemarkDict p WHERE p.type = :type")
  Integer findMaxSortByType(@Param("type") Integer type);
}
