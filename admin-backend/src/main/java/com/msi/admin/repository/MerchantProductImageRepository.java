package com.msi.admin.repository;

import com.msi.admin.domain.MerchantProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantProductImageRepository extends JpaRepository<MerchantProductImage, Long> {
    List<MerchantProductImage> findByProductIdAndIsValid(Long productId, Integer isValid);
}
