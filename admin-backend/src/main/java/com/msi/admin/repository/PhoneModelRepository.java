package com.msi.admin.repository;

import com.msi.admin.domain.PhoneModel;
import com.msi.admin.domain.PhoneSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhoneModelRepository extends JpaRepository<PhoneModel, Long> {
  List<PhoneModel> findBySeriesId(Long seriesId);

  Optional<PhoneModel> findBySeriesIdAndModelName(Long seriesId, String modelName);

  @Query("SELECT MAX(m.sort) FROM PhoneModel m WHERE m.seriesId = :seriesId")
  Integer findMaxSortBySeriesId(@Param("seriesId") Long seriesId);

  @Query("SELECT MAX(m.id) FROM PhoneModel m")
  Long findMaxId();
}

