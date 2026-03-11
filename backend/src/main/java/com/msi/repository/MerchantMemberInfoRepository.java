package com.msi.repository;

import com.msi.domain.MerchantMemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MerchantMemberInfoRepository extends JpaRepository<MerchantMemberInfo, Long> {
    java.util.Optional<MerchantMemberInfo> findByMerchantId(Long merchantId);

    long countByStartDateBetween(LocalDateTime start, LocalDateTime end);
    
    long countByIsValidAndEndDateAfter(Integer isValid, LocalDateTime now);
}
