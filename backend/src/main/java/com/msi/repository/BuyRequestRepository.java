package com.msi.repository;

import com.msi.domain.BuyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyRequestRepository extends JpaRepository<BuyRequest, Long>, JpaSpecificationExecutor<BuyRequest> {
  Page<BuyRequest> findByMerchantIdAndIsValid(Long merchantId, Integer isValid, Pageable pageable);
  java.util.Optional<BuyRequest> findFirstByMerchantIdAndBrandIdAndSeriesIdAndModelIdAndSpecIdAndIsValid(
      Long merchantId, Long brandId, Long seriesId, Long modelId, Long specId, Integer isValid);
  java.util.Optional<BuyRequest> findByIdAndIsValid(Long id, Integer isValid);
  Page<BuyRequest> findByIsValidAndState(Integer isValid, Integer state, Pageable pageable);
  Page<BuyRequest> findByCityCodeAndIsValidAndState(String cityCode, Integer isValid, Integer state, Pageable pageable);
}
