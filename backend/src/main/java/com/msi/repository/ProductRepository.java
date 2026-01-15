package com.msi.repository;

import com.msi.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  Page<Product> findByMerchantIdAndIsValid(Long merchantId, Integer isValid, Pageable pageable);

  Page<Product> findByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
      Long merchantId, Long brandId, Long seriesId, Long modelId, Long specId, Integer isValid, Pageable pageable);

  java.util.Optional<Product> findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
      Long merchantId, Long brandId, Long seriesId, Long modelId, Long specId, Integer isValid);

  java.util.Optional<Product> findByIdAndIsValid(Long id, Integer isValid);
}
