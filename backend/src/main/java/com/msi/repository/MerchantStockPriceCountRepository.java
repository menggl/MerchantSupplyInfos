package com.msi.repository;

import com.msi.domain.MerchantStockPriceCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantStockPriceCountRepository extends JpaRepository<MerchantStockPriceCount, Long>, JpaSpecificationExecutor<MerchantStockPriceCount> {
}
