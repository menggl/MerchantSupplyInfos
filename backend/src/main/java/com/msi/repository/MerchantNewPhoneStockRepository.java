package com.msi.repository;

import com.msi.domain.MerchantNewPhoneStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantNewPhoneStockRepository extends JpaRepository<MerchantNewPhoneStock, Long>, JpaSpecificationExecutor<MerchantNewPhoneStock>, MerchantNewPhoneStockRepositoryCustom {
}
