package com.msi.admin.repository;

import com.msi.admin.domain.MerchantPhoneProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantPhoneProductRepository extends JpaRepository<MerchantPhoneProduct, Long>, JpaSpecificationExecutor<MerchantPhoneProduct> {
}
