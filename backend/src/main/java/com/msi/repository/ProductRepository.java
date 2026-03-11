package com.msi.repository;

import com.msi.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface ProductRepository extends JpaSpecificationExecutor<Product>, JpaRepository<Product, Long>, ProductRepositoryCustom {
  Page<Product> findByMerchantIdAndIsValid(Long merchantId, Integer isValid, Pageable pageable);

  Page<Product> findByMerchantIdAndProductTypeAndIsValid(Long merchantId, Integer productType, Integer isValid, Pageable pageable);

  Page<Product> findByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
      Long merchantId, Long brandId, Long seriesId, Long modelId, Long specId, Integer isValid, Pageable pageable);

  java.util.Optional<Product> findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
      Long merchantId, Long brandId, Long seriesId, Long modelId, Long specId, Integer isValid);

  java.util.Optional<Product> findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndProductTypeAndIsValid(
      Long merchantId, Long brandId, Long seriesId, Long modelId, Long specId, Integer productType, Integer isValid);

  java.util.Optional<Product> findByIdAndIsValid(Long id, Integer isValid);

  long countByProductTypeAndIsValidAndStateAndCreateTimeBetween(Integer type, Integer isValid, Integer state, java.time.LocalDateTime start, java.time.LocalDateTime end);
  
  long countByProductTypeAndIsValidAndState(Integer type, Integer isValid, Integer state);

  @Query("SELECT COUNT(p) FROM Product p WHERE p.productType = :type AND p.isValid = :isValid AND p.state = :state AND p.updateTime BETWEEN :start AND :end AND p.createTime < :start")
  long countUpdatedProducts(@Param("type") Integer type, @Param("isValid") Integer isValid, @Param("state") Integer state, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COUNT(p) FROM Product p, Merchant m WHERE p.merchantId = m.id AND p.productType = :productType AND p.isValid = 1 AND p.state = 1 AND p.createTime BETWEEN :start AND :end AND m.createTime BETWEEN :start AND :end AND m.merchantPhone IS NOT NULL")
  long countNewUserProducts(@Param("productType") Integer productType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COUNT(p) FROM Product p, Merchant m WHERE p.merchantId = m.id AND p.productType = :productType AND p.isValid = 1 AND p.state = 1 AND p.createTime BETWEEN :start AND :end AND m.createTime < :start AND m.merchantPhone IS NOT NULL")
  long countOldUserProducts(@Param("productType") Integer productType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COUNT(p) FROM Product p, Merchant m WHERE p.merchantId = m.id AND p.productType = :productType AND p.isValid = 1 AND p.state = 1 AND p.updateTime BETWEEN :start AND :end AND p.createTime < :start AND m.createTime BETWEEN :start AND :end AND m.merchantPhone IS NOT NULL")
  long countNewUserProductUpdates(@Param("productType") Integer productType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("SELECT COUNT(p) FROM Product p, Merchant m WHERE p.merchantId = m.id AND p.productType = :productType AND p.isValid = 1 AND p.state = 1 AND p.updateTime BETWEEN :start AND :end AND p.createTime < :start AND m.createTime < :start AND m.merchantPhone IS NOT NULL")
  long countOldUserProductUpdates(@Param("productType") Integer productType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Modifying
  @Query("UPDATE Product p SET p.updateTime = :updateTime WHERE p.merchantId = :merchantId AND p.productType = :productType AND p.isValid = 1")
  void updateProductTime(@Param("merchantId") Long merchantId, @Param("productType") Integer productType, @Param("updateTime") LocalDateTime updateTime);

  @Modifying
  @Query("UPDATE Product p SET p.updateTime = :updateTime WHERE p.merchantId = :merchantId AND p.isValid = 1")
  void updateAllProductTime(@Param("merchantId") Long merchantId, @Param("updateTime") LocalDateTime updateTime);
}
