package com.msi.admin.repository;

import com.msi.admin.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
  Optional<Brand> findByName(String name);

  @Query("SELECT MAX(b.sort) FROM Brand b")
  Integer findMaxSort();

  @Query("SELECT MAX(b.id) FROM Brand b")
  Long findMaxId();

  @Query(value = "SELECT id, name, sort, deleted FROM brand WHERE deleted IS NULL OR deleted = 0 ORDER BY IFNULL(sort,0)", nativeQuery = true)
  java.util.List<Brand> findAllValidOrderBySort();
}
