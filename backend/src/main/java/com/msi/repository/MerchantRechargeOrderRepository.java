package com.msi.repository;

import com.msi.domain.MerchantRechargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantRechargeOrderRepository extends JpaRepository<MerchantRechargeOrder, Long> {
  Optional<MerchantRechargeOrder> findByOrderNo(String orderNo);
}

