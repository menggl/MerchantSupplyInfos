package com.msi.repository;

import com.msi.domain.PhoneRemarkDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneRemarkDictRepository extends JpaRepository<PhoneRemarkDict, Long> {
  List<PhoneRemarkDict> findByTypeAndValidOrderBySortAsc(Integer type, Integer valid);
}
