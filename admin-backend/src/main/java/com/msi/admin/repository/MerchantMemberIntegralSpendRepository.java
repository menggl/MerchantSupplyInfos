package com.msi.admin.repository;

import com.msi.admin.domain.MerchantMemberIntegralSpend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MerchantMemberIntegralSpendRepository extends JpaRepository<MerchantMemberIntegralSpend, Long> {
    List<MerchantMemberIntegralSpend> findByMerchantIdOrderByChangeTimeDesc(Long merchantId);
}
