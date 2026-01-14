package com.msi.admin.repository;

import com.msi.admin.domain.MerchantMemberRecharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantMemberRechargeRepository extends JpaRepository<MerchantMemberRecharge, Long> {
    List<MerchantMemberRecharge> findByMerchantIdOrderByRechargeTimeDesc(Long merchantId);
}
