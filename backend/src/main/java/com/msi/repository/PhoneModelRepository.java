package com.msi.repository;

import com.msi.domain.PhoneModel;
import com.msi.domain.PhoneSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface PhoneModelRepository extends JpaRepository<PhoneModel, Long> {
  List<PhoneModel> findBySeriesOrderBySortAsc(PhoneSeries series);
  Optional<PhoneModel> findBySeriesAndModelName(PhoneSeries series, String modelName);
}
