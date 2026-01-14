package com.msi.repository;

import com.msi.domain.MerchantMemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantMemberInfoRepository extends JpaRepository<MerchantMemberInfo, Long> {
  java.util.Optional<MerchantMemberInfo> findByMerchant_Id(Long merchantId);
}
