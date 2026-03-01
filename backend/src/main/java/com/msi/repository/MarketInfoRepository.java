package com.msi.repository;

import com.msi.domain.MarketInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketInfoRepository extends JpaRepository<MarketInfo, Long> {
    Page<MarketInfo> findByIsOnlineAndIsDeleted(Integer isOnline, Integer isDeleted, Pageable pageable);
    
    Optional<MarketInfo> findByIdAndIsOnlineAndIsDeleted(Long id, Integer isOnline, Integer isDeleted);

    List<MarketInfo> findTop3ByIsOnlineAndIsDeletedOrderBySortAsc(Integer isOnline, Integer isDeleted);
}
