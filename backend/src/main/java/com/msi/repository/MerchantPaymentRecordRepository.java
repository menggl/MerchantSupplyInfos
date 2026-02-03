package com.msi.repository;

import com.msi.domain.MerchantPaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantPaymentRecordRepository extends JpaRepository<MerchantPaymentRecord, Long>, JpaSpecificationExecutor<MerchantPaymentRecord> {
}
