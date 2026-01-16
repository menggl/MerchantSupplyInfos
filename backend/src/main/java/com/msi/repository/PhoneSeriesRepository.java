package com.msi.repository;

import com.msi.domain.Brand;
import com.msi.domain.PhoneSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface PhoneSeriesRepository extends JpaRepository<PhoneSeries, Long> {
  List<PhoneSeries> findByBrandOrderBySortAsc(Brand brand);
  Optional<PhoneSeries> findByBrandAndSeriesName(Brand brand, String seriesName);
}

