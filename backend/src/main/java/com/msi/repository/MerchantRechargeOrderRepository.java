package com.msi.repository;

import com.msi.domain.MerchantRechargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRechargeOrderRepository extends JpaRepository<MerchantRechargeOrder, Long> {
  Optional<MerchantRechargeOrder> findByOrderNo(String orderNo);

  @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM MerchantRechargeOrder o WHERE o.payStatus = :payStatus AND o.createTime <= :end")
  Long sumTotalAmountByPayStatusAndCreateTimeLessThanEqual(@org.springframework.data.repository.query.Param("payStatus") Integer payStatus, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}

