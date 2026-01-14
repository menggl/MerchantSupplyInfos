package com.msi.repository;

import com.msi.domain.PhoneModel;
import com.msi.domain.PhoneSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface PhoneSpecRepository extends JpaRepository<PhoneSpec, Long> {
  List<PhoneSpec> findByModel(PhoneModel model);
  Optional<PhoneSpec> findByModelAndSpecName(PhoneModel model, String specName);
}
