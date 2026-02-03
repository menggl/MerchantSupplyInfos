package com.msi.repository;

import com.msi.domain.MerchantBrandMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantBrandMapRepository extends JpaRepository<MerchantBrandMap, Long>, JpaSpecificationExecutor<MerchantBrandMap> {
}
