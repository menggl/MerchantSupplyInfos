package com.msi.repository;

import com.msi.domain.MerchantMemberIntegralSpend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantMemberIntegralSpendRepository extends JpaRepository<MerchantMemberIntegralSpend, Long> {
  long countByMerchantIdAndChangeReasonAndChangeTimeBetween(Long merchantId,
                                                            String changeReason,
                                                            LocalDateTime startTime,
                                                            LocalDateTime endTime);

  long countByChangeReasonAndChangeTimeBetween(String changeReason, LocalDateTime start, LocalDateTime end);

  @Query("SELECT COALESCE(SUM(s.changeAmount), 0) FROM MerchantMemberIntegralSpend s WHERE s.changeReason = :reason AND s.changeTime BETWEEN :start AND :end")
  long sumChangeAmountByChangeReasonAndChangeTimeBetween(@Param("reason") String reason, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
