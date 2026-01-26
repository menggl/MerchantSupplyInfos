package com.msi.repository;

import com.msi.domain.MerchantSearchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantSearchRecordRepository extends JpaRepository<MerchantSearchRecord, Long> {
}
