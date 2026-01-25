package com.msi.admin.repository;

import com.msi.admin.domain.PhoneModel;
import com.msi.admin.domain.PhoneSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhoneSpecRepository extends JpaRepository<PhoneSpec, Long> {
  List<PhoneSpec> findByModelId(Long modelId);
  Optional<PhoneSpec> findByModelIdAndSpecName(Long modelId, String specName);

  @Query("SELECT MAX(s.sort) FROM PhoneSpec s WHERE s.modelId = :modelId")
  Integer findMaxSortByModelId(@Param("modelId") Long modelId);

  @Query("SELECT MAX(s.id) FROM PhoneSpec s")
  Long findMaxId();
}

