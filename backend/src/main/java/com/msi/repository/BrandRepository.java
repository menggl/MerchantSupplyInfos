package com.msi.repository;

import com.msi.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
  Optional<Brand> findByName(String name);
}

