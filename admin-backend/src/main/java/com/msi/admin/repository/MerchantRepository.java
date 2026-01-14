package com.msi.admin.repository;

import com.msi.admin.domain.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {
    List<Merchant> findAllByOrderByIdDesc();
    
    List<Merchant> findByMerchantNameContaining(String merchantName);
}

