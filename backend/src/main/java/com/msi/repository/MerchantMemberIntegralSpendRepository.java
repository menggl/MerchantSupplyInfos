package com.msi.repository;

import com.msi.domain.MerchantMemberIntegralSpend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface MerchantMemberIntegralSpendRepository extends JpaRepository<MerchantMemberIntegralSpend, Long> {
  long countByMerchantIdAndChangeReasonAndChangeTimeBetween(Long merchantId,
                                                            String changeReason,
                                                            LocalDateTime startTime,
                                                            LocalDateTime endTime);
}
