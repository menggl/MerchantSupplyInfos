package com.msi.admin.repository;

import com.msi.admin.domain.MerchantMemberIntegral;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MerchantMemberIntegralRepository extends JpaRepository<MerchantMemberIntegral, Long> {
    Optional<MerchantMemberIntegral> findByMerchantId(Long merchantId);
}
