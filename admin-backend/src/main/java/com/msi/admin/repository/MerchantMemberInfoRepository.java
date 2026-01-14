package com.msi.admin.repository;

import com.msi.admin.domain.MerchantMemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantMemberInfoRepository extends JpaRepository<MerchantMemberInfo, Long> {
    Optional<MerchantMemberInfo> findTopByMerchant_IdAndIsValidOrderByStartDateDesc(Long merchantId, Integer isValid);

    java.util.List<MerchantMemberInfo> findByMerchant_IdOrderByStartDateDesc(Long merchantId);

    void deleteByMerchant_Id(Long merchantId);
}

