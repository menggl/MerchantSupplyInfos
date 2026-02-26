package com.msi.repository;

import com.msi.domain.MerchantCallRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MerchantCallRecordRepository extends JpaRepository<MerchantCallRecord, Long> {
    long countByCallTypeAndCreateTimeBetween(Integer callType, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT r.callerMerchantId) FROM MerchantCallRecord r WHERE r.callType = :callType AND r.createTime BETWEEN :start AND :end")
    long countDistinctCallerMerchantIdByCallTypeAndCreateTimeBetween(@Param("callType") Integer callType, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}

