package com.msi.repository;

import com.msi.domain.MerchantSecondPhoneStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantSecondPhoneStockRepository extends JpaRepository<MerchantSecondPhoneStock, Long>, JpaSpecificationExecutor<MerchantSecondPhoneStock> {
}
