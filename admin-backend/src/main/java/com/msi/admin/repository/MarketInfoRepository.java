package com.msi.admin.repository;

import com.msi.admin.domain.MarketInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketInfoRepository extends JpaRepository<MarketInfo, Long> {
    Page<MarketInfo> findByIsDeleted(Integer isDeleted, Pageable pageable);
    
    Page<MarketInfo> findByIsDeletedAndTitleContaining(Integer isDeleted, String title, Pageable pageable);
}
