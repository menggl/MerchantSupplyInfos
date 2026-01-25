package com.msi.admin.repository;

import com.msi.admin.domain.MerchantRechargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRechargeOrderRepository extends JpaRepository<MerchantRechargeOrder, Long> {
    List<MerchantRechargeOrder> findByMerchantIdOrderByCreateTimeDesc(Long merchantId);
}
