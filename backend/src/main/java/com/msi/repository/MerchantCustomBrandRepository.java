package com.msi.repository;

import com.msi.domain.MerchantCustomBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantCustomBrandRepository extends JpaRepository<MerchantCustomBrand, Long>, JpaSpecificationExecutor<MerchantCustomBrand> {
}
