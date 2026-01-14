package com.msi.admin.repository;

import com.msi.admin.domain.Brand;
import com.msi.admin.domain.PhoneSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhoneSeriesRepository extends JpaRepository<PhoneSeries, Long> {
  List<PhoneSeries> findByBrand(Brand brand);
  
  Optional<PhoneSeries> findByBrandAndSeriesName(Brand brand, String seriesName);

  @Query("SELECT MAX(s.sort) FROM PhoneSeries s WHERE s.brand.id = :brandId")
  Integer findMaxSortByBrandId(@Param("brandId") Long brandId);

  @Query("SELECT MAX(s.id) FROM PhoneSeries s")
  Long findMaxId();
}

