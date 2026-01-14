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
}

